package ch.kk7.config4j.binding;

import ch.kk7.config4j.binding.collection.CollectionBindingFactory;
import ch.kk7.config4j.binding.intf.InterfaceBindingFactory;
import ch.kk7.config4j.binding.leaf.LeafBinding.AnnotatedLeafBindingFactory;
import ch.kk7.config4j.binding.leaf.LeafBinding.LeafBindingFactory;
import ch.kk7.config4j.binding.leaf.ValueMapperFactory;
import ch.kk7.config4j.binding.leaf.mapper.DefaultValueMapperFactory;
import ch.kk7.config4j.binding.map.MapBindingFactory;
import ch.kk7.config4j.common.Config4jException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigBinder {
	private List<ConfigBindingFactory<?>> descriptionFactories;

	public ConfigBinder() {
		this(new DefaultValueMapperFactory());
	}

	public ConfigBinder(ValueMapperFactory valueMapperFactory) {
		// order is important here
		descriptionFactories = new ArrayList<>();
		// @ValueMapper annotations have preferences (since they can also bind all following types)
		descriptionFactories.add(new AnnotatedLeafBindingFactory());
		// collection and map before interface (since they are themselves interfaces)
		descriptionFactories.add(new CollectionBindingFactory());
		descriptionFactories.add(new MapBindingFactory());
		descriptionFactories.add(new InterfaceBindingFactory());
		descriptionFactories.add(new LeafBindingFactory(valueMapperFactory));
	}

	@SuppressWarnings("unchecked")
	public <T> ConfigBinding<T> toRootConfigBinding(Class<T> forClass) {
		return (ConfigBinding<T>) toConfigBinding(BindingType.newBindingType(forClass));
	}

	public ConfigBinding<?> toConfigBinding(BindingType type) {
		return descriptionFactories.stream()
				.map(configDescriptionFactory -> configDescriptionFactory.maybeCreate(type, this))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new Config4jException("cannot handle file " + type));
	}
}
