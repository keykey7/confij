package ch.kk7.confij.binding.map;

import ch.kk7.confij.binding.ConfijBindingException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

@Value
@NonFinal
public class MapBuilder {
	private final Supplier<Map> supplier;
	private final Function<Map, Map> hardener;

	public MapBuilder(ResolvedType type) {
		if (!type.isInstanceOf(Map.class)) {
			throw new IllegalArgumentException("expected a map type, but got " + type);
		}
		supplier = newMapSupplier(type);
		hardener = newMapHardener(type);
	}

	protected Supplier<Map> newMapSupplier(ResolvedType type) {
		if (type.isInterface()) {
			return interfaceSupplier(type);
		} else {
			return constructorSupplier(type);
		}
	}

	protected Supplier<Map> interfaceSupplier(ResolvedType type) {
		Class<?> intfClass = type.getErasedType();
		if (intfClass.isAssignableFrom(HashMap.class)) {
			return HashMap::new;
		} else if (intfClass.isAssignableFrom(TreeMap.class)) {
			return TreeMap::new;
		} else {
			throw new ConfijBindingException("Attempting to bind to a Map of interface-type {}. " +
					"However no supported implementation is known for this. Prefer Map directly.", type);
		}
	}

	protected Supplier<Map> constructorSupplier(ResolvedType type) {
		@SuppressWarnings("unchecked")
		Constructor<Map> constructor = (Constructor<Map>) type.getConstructors()
				.stream()
				.map(RawConstructor::getRawMember)
				.filter(c -> c.getParameterCount() == 0)
				.findAny()
				.orElseThrow(() -> new ConfijBindingException("Attempted to bind to a Map of type {}. " +
						"However this class doesn't provide a no-arg constructor. " +
						"It's preferable to use a tree Map interface " +
						"instead of concrete Map classes.", type));
		return () -> {
			try {
				return constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new ConfijBindingException("unable to call no-arg constructor on {}", type, e);
			}
		};
	}

	protected Function<Map, Map> newMapHardener(ResolvedType type) {
		Class<?> intfClass = type.getErasedType();
		if (Map.class.equals(intfClass)) {
			return x -> Collections.unmodifiableMap((Map<?, ?>) x);
		} else if (SortedMap.class.equals(intfClass)) {
			return x -> Collections.unmodifiableSortedMap((SortedMap<?, ?>) x);
		} else if (NavigableMap.class.equals(intfClass)) {
			return x -> Collections.unmodifiableNavigableMap((NavigableMap<?, ?>) x);
		}
		// otherwise no hardening supported
		return x -> x;
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> newInstance() {
		return (Map<K, V>) supplier.get();
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> tryHarden(Map<K, V> collection) {
		return (Map<K, V>) hardener.apply(collection);
	}
}
