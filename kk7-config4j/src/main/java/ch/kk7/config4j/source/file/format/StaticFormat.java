package ch.kk7.config4j.source.file.format;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StaticFormat {
	public static <K, V> Entry<K, V> e(K k, V v) {
		return new Entry<>() {
			@Override
			public K getKey() {
				return k;
			}

			@Override
			public V getValue() {
				return v;
			}

			@Override
			public V setValue(V v) {
				throw new RuntimeException();
			}
		};
	}

	@SafeVarargs
	public static <K, V> Map<K, V> map(Entry<K, V>... entries) {
		Map<K, V> map = new LinkedHashMap<>();
		Arrays.stream(entries).forEachOrdered(e -> map.put(e.getKey(), e.getValue()));
		return Collections.unmodifiableMap(map);
	}

	@SafeVarargs
	public static <E> List<E> list(E... elements) {
		return Arrays.asList(elements);
	}
}
