package ch.kk7.config4j.binding.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;

public class UnmodifiableCollectionBuilder<E, T extends Collection<E>> {
	private final Supplier<T> supplier;
	private final Class<? extends Collection> instanceClass;
	private final Function<T, T> hardener;

	public UnmodifiableCollectionBuilder(Supplier<T> supplier, Function<T, T> hardener) {
		this.supplier = supplier;
		this.hardener = hardener;
		instanceClass = hardener.apply(supplier.get())
				.getClass();
	}

	public Class<? extends Collection> getInstanceClass() {
		return instanceClass;
	}

	public T getModifyableInstance() {
		return supplier.get();
	}

	public T harden(T intermediate) {
		return hardener.apply(intermediate);
	}

	public static <E> UnmodifiableCollectionBuilder<E, Set<E>> setBuilder() {
		return new UnmodifiableCollectionBuilder<>(CopyOnWriteArraySet::new, Collections::unmodifiableSet);
	}

	public static <E> UnmodifiableCollectionBuilder<E, List<E>> listBuilder() {
		return new UnmodifiableCollectionBuilder<>(CopyOnWriteArrayList::new, Collections::unmodifiableList);
	}
}
