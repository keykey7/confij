package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawConstructor;

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

/**
 * Given a Collection Type, this class is responsible to provide new instances and make these instances unmodifyable if possible.
 */
public class CollectionBuilder {
	private final Supplier<Collection> supplier;
	private final Function<Collection, Collection> hardener;

	public CollectionBuilder(ResolvedType type) {
		if (!type.isInstanceOf(Collection.class)) {
			throw new IllegalArgumentException("expected a collection type, but got " + type);
		}
		supplier = newCollectionSupplier(type);
		hardener = newCollectionHardener(type);
	}

	protected Function<Collection, Collection> newCollectionHardener(ResolvedType type) {
		Class<?> intfClass = type.getErasedType();
		if (Set.class.equals(intfClass)) {
			return x -> Collections.unmodifiableSet((Set<?>) x);
		} else if (List.class.equals(intfClass)) {
			return x -> Collections.unmodifiableList((List<?>) x);
		} else if (SortedSet.class.equals(intfClass)) {
			return x -> Collections.unmodifiableSortedSet((SortedSet<?>) x);
		} else if (NavigableSet.class.equals(intfClass)) {
			return x -> Collections.unmodifiableNavigableSet((NavigableSet<?>) x);
		}
		// otherwise no hardening supported
		return x -> x;
	}

	protected Supplier<Collection> newCollectionSupplier(ResolvedType type) {
		if (type.isInterface()) {
			return interfaceSupplier(type);
		} else {
			return constructorSupplier(type);
		}
	}

	protected Supplier<Collection> interfaceSupplier(ResolvedType type) {
		Class<?> intfClass = type.getErasedType();
		if (intfClass.isAssignableFrom(LinkedHashSet.class)) {
			return LinkedHashSet::new;
		} else if (intfClass.isAssignableFrom(ArrayList.class)) {
			return ArrayList::new;
		} else if (intfClass.isAssignableFrom(TreeSet.class)) {
			return TreeSet::new;
		} else {
			throw new BindingException("Attempting to bind to a Collection of interface-type {}. " +
					"However no supported implementation is known for this. Prefer Set or List directly.", type);
		}
	}

	protected Supplier<Collection> constructorSupplier(ResolvedType type) {
		@SuppressWarnings("unchecked")
		Constructor<Collection> constructor = (Constructor<Collection>) type.getConstructors()
				.stream()
				.map(RawConstructor::getRawMember)
				.filter(c -> c.getParameterCount() == 0)
				.findAny()
				.orElseThrow(() -> new BindingException("Attempted to bind to a Collection of type {}. " +
						"However this class doesn't provide a no-arg constructor. " +
						"It's preferable to use simple Set or List interfaces " +
						"instead of concrete Collection classes.", type));
		return () -> {
			try {
				return constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new BindingException("unable to call no-arg constructor on {}", type, e);
			}
		};
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
