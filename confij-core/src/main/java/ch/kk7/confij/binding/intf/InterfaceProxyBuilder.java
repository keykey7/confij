package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.common.Util;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class InterfaceProxyBuilder<T> {
	private final static Map<Class<?>, Object> PRIMITIVE_ZEROS = Stream.of(boolean.class, byte.class, char.class, double.class, float.class,
			int.class, long.class, short.class)
			.collect(toMap(clazz -> (Class<?>) clazz, clazz -> Array.get(Array.newInstance(clazz, 1), 0)));
	@Getter
	private final ResolvedInterfaceType type;
	@Getter
	private final Set<ResolvedMethod> allowedMethods;
	private final Set<ResolvedMethod> mandatoryMethods;

	public interface Config4jHandled {
		Map<Method, Object> methodToValue();
	}

	public InterfaceProxyBuilder(ResolvedInterfaceType type) {
		this.type = type;
		allowedMethods = supportedMethods(false);
		mandatoryMethods = supportedMethods(true);
	}

	protected Set<ResolvedMethod> supportedMethods(boolean mandatoryOnly) {
		ResolvedMethod[] memberMethodsArr = supportedMemberResolver(mandatoryOnly).resolve(type, null, null)
				.getMemberMethods();
		return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(memberMethodsArr)));
	}

	protected MemberResolver supportedMemberResolver(boolean mandatoryOnly) {
		// TODO: use the common typeResolver
		MemberResolver memberResolver = new MemberResolver(new TypeResolver());
		memberResolver.setMethodFilter(method -> {
			if (Util.rawObjectType.equals(method.getDeclaringType())) {
				return false;
			}
			Method rawMethod = method.getRawMember();
			if (rawMethod.getParameterCount() != 0) {
				if (rawMethod.isDefault()) {
					return false;
				}
				throw new Config4jException("expected no-arg methods only, but found " + method);
			}
			if (mandatoryOnly && rawMethod.isDefault()) {
				return false;
			}
			return true;
		});
		return memberResolver;
	}

	@SuppressWarnings("unchecked")
	protected static <T> T classToPrimitive(Class<T> primitiveClass) {
		return (T) PRIMITIVE_ZEROS.get(primitiveClass);
	}

	public ValidatingProxyBuilder builder() {
		return new ValidatingProxyBuilder();
	}

	public class ValidatingProxyBuilder {
		private final Map<ResolvedMethod, Object> methodToValues = new HashMap<>();

		public ValidatingProxyBuilder methodToValue(ResolvedMethod resolvedMethod, Object value) {
			methodToValues.put(resolvedMethod, value);
			return this;
		}

		public T build() {
			Set<ResolvedMethod> inputMethods = methodToValues.keySet();
			Set<ResolvedMethod> notAllowedMethods = new HashSet<>(inputMethods);
			notAllowedMethods.removeAll(allowedMethods);
			if (!notAllowedMethods.isEmpty()) {
				throw new BindingException("cannot create instance of type '{}' with methods {}. allowed are {}", type, notAllowedMethods,
						allowedMethods);
			}
			Set<ResolvedMethod> missingMandatoryMethods = new HashSet<>(mandatoryMethods);
			missingMandatoryMethods.removeAll(inputMethods);
			if (!missingMandatoryMethods.isEmpty()) {
				throw new BindingException("cannot create instance of type '{}' due to missing mandatory methods {}", type,
						missingMandatoryMethods);
			}
			// input methods are valid at this point
			Map<Method, Object> fixedMethodToValue = new HashMap<>();
			methodToValues.forEach((method, value) -> {
				ResolvedType returnClass = method.getReturnType();
				if (value == null && returnClass.isPrimitive()) {
					// handle default values for primitive types and avoid NPE when accessing them
					value = classToPrimitive(returnClass.getErasedType());
				}
				fixedMethodToValue.put(method.getRawMember(), value);
			});
			Class forInterface = type.getErasedType();
			IntfaceInvocationHandler invocationHandler = new IntfaceInvocationHandler(forInterface.getSimpleName(), fixedMethodToValue);
			//noinspection unchecked
			return (T) Proxy.newProxyInstance(forInterface.getClassLoader(), new Class[]{forInterface, Config4jHandled.class},
					invocationHandler);
		}
	}
}
