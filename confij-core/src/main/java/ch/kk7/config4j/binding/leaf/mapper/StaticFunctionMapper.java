package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.leaf.IValueMapper;
import ch.kk7.config4j.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.config4j.binding.leaf.IValueMapperFactory;
import ch.kk7.config4j.common.Config4jException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StaticFunctionMapper<T> implements NullableValueMapper<T> {
	private final Method method;

	protected StaticFunctionMapper(Method method) {
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

	public static class StaticFunctionMapperFactory implements IValueMapperFactory {
		private List<String> expectedMethodNames = Arrays.asList("valueOf", "fromString");

		@Override
		public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
			ResolvedType type = bindingType.getResolvedType();
			return type.getStaticMethods()
					.stream()
					.map(RawMethod::getRawMember)
					.filter(StaticFunctionMapperFactory::isCallableMethod)
					.filter(method -> expectedMethodNames.contains(method.getName()))
					.findFirst()
					.map(StaticFunctionMapper::new);
		}

		public static boolean isCallableMethod(Method method) {
			return Modifier.isStatic(method.getModifiers()) &&
					method.getParameterCount() == 1 &&
					String.class.equals(method.getParameterTypes()[0]) &&
					method.getDeclaringClass()
							.isAssignableFrom(method.getReturnType());
		}
	}
}
