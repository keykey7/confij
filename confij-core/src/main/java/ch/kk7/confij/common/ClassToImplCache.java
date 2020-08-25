package ch.kk7.confij.common;

import lombok.ToString;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * given a class with an empty constructor, maintain a cache for class→instance without any guarantees.
 */
@ToString
@UtilityClass
@SuppressWarnings("java:S3011")
public class ClassToImplCache {

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

	/**
	 * Clear all the class caches.
	 */
    public void clear() {
	    instances.clear();
    }
}
