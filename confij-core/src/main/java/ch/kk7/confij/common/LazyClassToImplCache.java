package ch.kk7.confij.common;

import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

@ToString
public class LazyClassToImplCache {
	private final Map<Class<?>, Object> instances = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> clazz) {
		return (T) instances.computeIfAbsent(clazz, k -> {
			try {
				Constructor<?> constructor = k.getDeclaredConstructor();
				if (!constructor.isAccessible()) {
					constructor.setAccessible(true);
				}
				return constructor.newInstance();
			} catch (Exception e) {
				throw new ConfijException("unable to instantiate: " + k, e);
			}
		});
	}

	public <T> T getInstance(Class<? extends T> clazz, Class<T> asClass) {
		return asClass.cast(getInstance(clazz));
	}
}
