package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.common.Config4jException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SoloConstructorMapper<T> implements ValueMapper<T> {

	private final Constructor<T> constructor;

	public SoloConstructorMapper(Constructor<T> constructor, Class<T> forClass) {
		if (!isValidConstructor(constructor, forClass)) {
			throw new IllegalArgumentException("not a valid construtor");
		}
		this.constructor = constructor;
	}

	public static <T> boolean isValidConstructor(Constructor<T> constructor, Class<T> forClass) {
		return forClass.equals(constructor.getDeclaringClass()) &&
				constructor.getParameterCount() == 1 &&
				String.class.equals(constructor.getParameterTypes()[0]);
	}

	@Override
	public T fromString(String string) {
		try {
			return constructor.newInstance(string);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new Config4jException("unable to instanitate obj using constructor " + constructor, e);
		}
	}
}
