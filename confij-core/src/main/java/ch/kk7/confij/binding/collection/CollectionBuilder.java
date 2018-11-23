package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingException;
import com.fasterxml.classmate.ResolvedType;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Given a Collection Type, this class is responsible to provide new instances and make these instances unmodifyable if possible.
 */
public class CollectionBuilder {
	private final Supplier<Collection> supplier;
	private final Function<Collection, Collection> hardener;

	public CollectionBuilder(ResolvedType type) {
		this(erasedCollectionType(type));
	}

	public CollectionBuilder(@NonNull Class<? extends Collection> collectionClass) {
		supplier = newCollectionSupplier(collectionClass);
		hardener = newCollectionHardener(collectionClass);
	}

	@SuppressWarnings("unchecked")
	protected static Class<? extends Collection> erasedCollectionType(ResolvedType type) {
		if (!type.isInstanceOf(Collection.class)) {
			throw new IllegalArgumentException("expected a collection type, but got " + type);
		}
		return (Class<? extends Collection>) type.getErasedType();
	}

	protected Supplier<Collection> newCollectionSupplier(Class<? extends Collection> collectionClass) {
		if (collectionClass.isInterface()) {
			return interfaceSupplier(collectionClass);
		} else {
			return constructorSupplier(collectionClass);
		}
	}

	protected Supplier<Collection> interfaceSupplier(Class<? extends Collection> collectionClass) {
		if (collectionClass.isAssignableFrom(LinkedHashSet.class)) {
			return LinkedHashSet::new;
		} else if (collectionClass.isAssignableFrom(ArrayList.class)) {
			return ArrayList::new;
		} else if (collectionClass.isAssignableFrom(TreeSet.class)) {
			return TreeSet::new;
		} else {
			throw new BindingException("Attempting to bind to a Collection of interface-type {}. " +
					"However no supported implementation is known for this. Prefer Set or List directly.", collectionClass);
		}
	}

	protected Supplier<Collection> constructorSupplier(Class<? extends Collection> collectionClass) {
		@SuppressWarnings("unchecked")
		Constructor<Collection> constructor = Stream.of(collectionClass.getConstructors())
				.map(x -> (Constructor<Collection>) x)
				.filter(c -> c.getParameterCount() == 0)
				.findAny()
				.orElseThrow(() -> new BindingException("Attempted to bind to a Collection of type {}. " +
						"However this class doesn't provide a no-arg constructor. " +
						"It's preferable to use tree Set or List interfaces " +
						"instead of concrete Collection classes.", collectionClass));
		return () -> {
			try {
				return constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new BindingException("unable to call no-arg constructor on {}", collectionClass, e);
			}
		};
	}

	protected Function<Collection, Collection> newCollectionHardener(Class<? extends Collection> collectionClass) {
		if (Set.class.equals(collectionClass)) {
			return x -> Collections.unmodifiableSet((Set<?>) x);
		} else if (List.class.equals(collectionClass)) {
			return x -> Collections.unmodifiableList((List<?>) x);
		} else if (SortedSet.class.equals(collectionClass)) {
			return x -> Collections.unmodifiableSortedSet((SortedSet<?>) x);
		} else if (NavigableSet.class.equals(collectionClass)) {
			return x -> Collections.unmodifiableNavigableSet((NavigableSet<?>) x);
		}
		// otherwise no hardening supported
		return x -> x;
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> newInstance() {
		return (Collection<T>) supplier.get();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> tryHarden(Collection<T> collection) {
		return (Collection<T>) hardener.apply(collection);
	}
}
