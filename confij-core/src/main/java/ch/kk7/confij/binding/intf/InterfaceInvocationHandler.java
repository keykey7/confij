package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.common.Util;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class InterfaceInvocationHandler<T> implements InvocationHandler {
	private final static Map<Class<?>, Object> PRIMITIVE_ZEROS = Stream.of(boolean.class, byte.class, char.class, double.class, float.class,
			int.class, long.class, short.class)
			.collect(toMap(clazz -> (Class<?>) clazz, clazz -> Array.get(Array.newInstance(clazz, 1), 0)));
	private final ResolvedInterfaceType type;
	private final List<ResolvedMethod> supportedMethods;
	private final Map<Method, Object> methodToValue;
	private final T instance;

	public interface Config4jHandled {
		Map<Method, Object> methodToValue();
	}

	public InterfaceInvocationHandler(ResolvedInterfaceType type) {
		this.type = type;
		supportedMethods = Arrays.asList(newMemberResolver().resolve(type, null, null)
				.getMemberMethods());
		methodToValue = new HashMap<>();
		instance = newInstance();
	}

	public ResolvedInterfaceType getType() {
		return type;
	}

	protected MemberResolver newMemberResolver() {
		// TODO: use the common typeResolver
		MemberResolver memberResolver = new MemberResolver(new TypeResolver());
		memberResolver.setMethodFilter(method -> {
			if (isIgnorableMethod(method)) {
				return false;
			}
			if (method.getRawMember()
					.getParameterCount() != 0) {
				throw new Config4jException("expected no-arg methods only, but found " + method);
			}
			return true;
		});
		return memberResolver;
	}

	public List<ResolvedMethod> getSupportedMethods() {
		return supportedMethods;
	}

	private static boolean isIgnorableMethod(RawMethod method) {
		Method raw = method.getRawMember();
		return Util.rawObjectType.equals(method.getDeclaringType()) ||
				method.isStatic() ||
				(raw.getParameterCount() != 0 && raw.isDefault());
	}

	public void clear() {
		methodToValue.clear();
	}

	public void setMethod(ResolvedMethod method, Object value) {
		if (!supportedMethods.contains(method)) {
			throw new IllegalArgumentException("method " + method + " isn't supported");
		}
		if (isEmpty(value)) {
			Method rawMethod = method.getRawMember();
			ResolvedType returnClass = method.getReturnType();
			if (rawMethod.isDefault()) {
				try {
					value = DefaultMethodHandler.invokeDefaultMethod(instance(), rawMethod, new Object[]{});
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new BindingException("failed to call default method on '{}()'", method, e);
				}
			} else if (returnClass.isPrimitive()) {
				// handle default values for primitive types and avoid NPE when accessing them
				value = classToPrimitive(returnClass.getErasedType());
			}
		}
		methodToValue.put(method.getRawMember(), value);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		if (Object.class.equals(method.getDeclaringClass())) {
			if ("toString".equals(method.getName())) {
				return intfToString();
			}
			return method.invoke(this, args);
		}
		if (Config4jHandled.class.equals(method.getDeclaringClass())) {
			return methodToValue;
		}
		if (!methodToValue.containsKey(method)) {
			if (Modifier.isStatic(method.getModifiers())) {
				return method.invoke(null, args);
			}
			throw new BindingException("cannot call method '{}' as it was not initialized. Known methods on this type are: {}", method,
					methodToValue.keySet());
		}
		return methodToValue.get(method);
	}

	// TODO: it's wrong to check here for empty values. this information should come from the source
	protected boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Collection) {
			return ((Collection) value).isEmpty();
		}
		if (value instanceof Map) {
			return ((Map) value).isEmpty();
		}
		if (value.getClass()
				.isArray()) {
			return Array.getLength(value) == 0;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected static <T> T classToPrimitive(Class<T> primitiveClass) {
		return (T) PRIMITIVE_ZEROS.get(primitiveClass);
	}

	protected String intfToString() {
		StringBuilder sb = new StringBuilder(type.getErasedType()
				.getSimpleName()).append("@")
				.append(Integer.toHexString(hashCode()))
				.append("{");
		methodToValue.forEach((k, v) -> sb.append(k.getName())
				.append("=")
				.append(v)
				.append(", "));
		sb.setLength(sb.length() - 2);
		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	protected T newInstance() {
		Class<T> forInterface = (Class<T>) type.getErasedType();
		return (T) Proxy.newProxyInstance(forInterface.getClassLoader(), new Class[]{forInterface, Config4jHandled.class}, this);
	}

	public T instance() {
		return instance;
	}
}
