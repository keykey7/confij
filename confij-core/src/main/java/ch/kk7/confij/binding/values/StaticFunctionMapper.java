package ch.kk7.confij.binding.values;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.values.ValueMapperInstance.NullableValueMapperInstance;
import ch.kk7.confij.common.Config4jException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

public class StaticFunctionMapper implements ValueMapperFactory {
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(StaticFunctionMapper.class)
	public @interface StaticFunction {
		String[] value() default {"valueOf", "fromString"};
	}

	@StaticFunction
	private static final class AnnonHolder {
	}

	public class StaticFunctionMapperInstance<T> implements NullableValueMapperInstance<T> {
		private final Method method;

		protected StaticFunctionMapperInstance(Method method) {
			this.method = method;
		}

		@Override
		public T fromNonNullString(String string) {
			try {
				//noinspection unchecked
				return (T) method.invoke(null, string);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new Config4jException("unable to instanitate obj using static method " + method, e);
			}
		}
	}

	public static boolean isCallableMethod(Method method) {
		return Modifier.isStatic(method.getModifiers()) &&
				method.getParameterCount() == 1 &&
				String.class.equals(method.getParameterTypes()[0]) &&
				method.getDeclaringClass()
						.isAssignableFrom(method.getReturnType());
	}

	public static boolean isExpectedMethodName(String methodName, BindingType bindingType) {
		return Arrays.asList(bindingType.getBindingSettings()
				.getFactoryConfigFor(StaticFunctionMapper.class)
				.filter(StaticFunction.class::isInstance)
				.map(StaticFunction.class::cast)
				.orElse(AnnonHolder.class.getAnnotation(StaticFunction.class))
				.value())
				.contains(methodName);
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		return type.getStaticMethods()
				.stream()
				.map(RawMethod::getRawMember)
				.filter(StaticFunctionMapper::isCallableMethod)
				.filter(method -> isExpectedMethodName(method.getName(), bindingType))
				.findFirst()
				.map(StaticFunctionMapperInstance::new);
	}
}
