package ch.kk7.confij.common;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class ServiceLoaderUtil {
	private static Map<Class<?>, List<?>> serviceInstances = new ConcurrentHashMap<>();

	public static <T> List<T> requireInstancesOf(Class<T> serviceClass) {
		List<T> services = instancesOf(serviceClass);
		if (services.isEmpty()) {
			throw new IllegalStateException("Failed to loadFrom any instance of " + serviceClass + ". Check your AnnotationProcessor.");
		}
		return services;
	}

	/**
	 * Loads and caches all instances of a given class using a {@link ServiceLoader} and
	 * sorts them by {@link ServiceLoaderPriority} first and by class name otherwise.
	 *
	 * @return list of initialized singletons of given type in deterministic order
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> instancesOf(Class<T> serviceClass) {
		// Note: not using computeIfAbsent due to Java8 bug: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8161372
		List<T> result = (List<T>) serviceInstances.get(serviceClass);
		if (result == null) {
			result = maybeNewOf(serviceClass);
			List<T> oldResult = (List<T>) serviceInstances.putIfAbsent(serviceClass, result);
			if (oldResult != null) {
				return oldResult;
			}
		}
		return result;
	}

	public static <T> List<T> maybeNewOf(Class<T> serviceClass) {
		ServiceLoader<T> resourceFormatLoader = ServiceLoader.load(serviceClass);
		return Collections.unmodifiableList(StreamSupport.stream(resourceFormatLoader.spliterator(), false)
				.sorted(Comparator.comparing(o -> o.getClass()
						.getName()))
				.sorted(Comparator.comparing(ServiceLoaderPriority::priorityOf)
						.reversed())
				.collect(Collectors.toList()));
	}
}
