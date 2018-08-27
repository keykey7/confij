package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.config4j.common.Config4jException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StaticFunctionMapper<T> implements NullableValueMapper<T> {
	private final Method method;

	public StaticFunctionMapper(Method method, Class<T> forClass) {
		if (!isValidMethod(method, forClass)) {
			throw new Config4jException("cannot use method " + method + " as string initializer");
		}
		this.method = method;
	}

	public static boolean isValidMethod(Method method, Class<?> forClass) {
		return Modifier.isStatic(method.getModifiers()) &&
				method.getParameterCount() == 1 &&
				String.class.equals(method.getParameterTypes()[0]) &&
				forClass.isAssignableFrom(method.getReturnType());
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
