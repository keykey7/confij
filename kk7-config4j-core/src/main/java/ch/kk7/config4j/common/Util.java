package ch.kk7.config4j.common;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedObjectType;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.kk7.config4j.source.simple.SimpleConfigException.classOf;

public class Util {

	public static final ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);

	private Util() {
		// util
	}

	public static <T> Collector<T, ?, T> singletonCollector() {
		return Collectors.collectingAndThen(
				Collectors.toList(),
				list -> {
					if (list.size() != 1) {
						throw new IllegalStateException();
					}
					return list.get(0);
				}
		);
	}

	public static <T, C> T assertClass(C instance, Class<T> clazz) {
		if (!clazz.isAssignableFrom(instance.getClass())) {
			throw new Config4jException("expected config to be of class {}", classOf(clazz));
		}
		//noinspection unchecked
		return (T) instance;
	}

	public static <T> Optional<T> firstOf(Optional<T>... optionals) {
		return Stream.of(optionals)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
}
