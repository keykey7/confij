package ch.kk7.config4j.binding.map;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class UnmodifiableMapBuilder<E, T extends Map<String, E>> {
	private final Supplier<T> supplier;
	private final Class<? extends Map> instanceClass;
	private final Function<T, T> hardener;

	public UnmodifiableMapBuilder(Supplier<T> supplier, Function<T, T> hardener) {
		this.supplier = supplier;
		this.hardener = hardener;
		instanceClass = hardener.apply(supplier.get())
				.getClass();
	}

	public Class<? extends Map> getInstanceClass() {
		return instanceClass;
	}

	public T getModifyableInstance() {
		return supplier.get();
	}

	public T harden(T intermediate) {
		return hardener.apply(intermediate);
	}

	public static <E> UnmodifiableMapBuilder<E, Map<String, E>> mapBuilder() {
		return new UnmodifiableMapBuilder<>(ConcurrentHashMap::new, Collections::unmodifiableMap);
	}
}
