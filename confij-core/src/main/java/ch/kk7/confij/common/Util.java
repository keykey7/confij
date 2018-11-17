package ch.kk7.confij.common;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class Util {

	public static final ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);

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

	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	public static <T> Optional<T> firstOf(Optional<T>... optionals) {
		return Stream.of(optionals)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
}
