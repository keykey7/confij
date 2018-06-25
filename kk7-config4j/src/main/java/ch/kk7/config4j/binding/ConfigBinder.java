package ch.kk7.config4j.binding;

import ch.kk7.config4j.binding.collection.CollectionBindingFactory;
import ch.kk7.config4j.binding.intf.InterfaceBindingFactory;
import ch.kk7.config4j.binding.leaf.LeafBinding.LeafBindingFactory;
import ch.kk7.config4j.binding.leaf.mapper.DefaultValueMapperFactory;
import ch.kk7.config4j.binding.leaf.mapper.ValueMapperFactory;
import ch.kk7.config4j.binding.map.MapBindingFactory;
import ch.kk7.config4j.common.Config4jException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigBinder {
	private TypeResolver typeResolver = new TypeResolver();
	private List<ConfigBindingFactory<?>> descriptionFactories;

	public ConfigBinder() {
		this(new DefaultValueMapperFactory());
	}

	public ConfigBinder(ValueMapperFactory valueMapperFactory) {
		// order is important here, since a List is also an interface
		// FIXME: checking for a custom annotated leaf is more important than collection and map factories...
		descriptionFactories = new ArrayList<>();
		descriptionFactories.add(new CollectionBindingFactory());
		descriptionFactories.add(new MapBindingFactory());
		descriptionFactories.add(new InterfaceBindingFactory());
		descriptionFactories.add(new LeafBindingFactory(valueMapperFactory));
	}

	@SuppressWarnings("unchecked")
	public <T> ConfigBinding<T> toConfigBinding(Class<T> type) {
		return (ConfigBinding<T>) toConfigBinding((Type) type);
	}

	public ConfigBinding<?> toConfigBinding(Type type) {
		return toConfigBinding(typeResolver.resolve(type));
	}

	public ConfigBinding<?> toConfigBinding(ResolvedType type) {
		return descriptionFactories.stream()
				.map(configDescriptionFactory -> configDescriptionFactory.maybeCreate(type, this))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new Config4jException("cannot handle file " + type));
	}
}
