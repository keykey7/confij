package ch.kk7.confij.binding;

import ch.kk7.confij.binding.array.ArrayBindingFactory;
import ch.kk7.confij.binding.collection.CollectionBindingFactory;
import ch.kk7.confij.binding.intf.InterfaceBindingFactory;
import ch.kk7.confij.binding.leaf.ForcedLeafBindingFactory;
import ch.kk7.confij.binding.leaf.LeafBindingFactory;
import ch.kk7.confij.binding.map.MapBindingFactory;
import ch.kk7.confij.common.ConfijException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigBinder {
	private List<ConfigBindingFactory<?>> descriptionFactories;

	public ConfigBinder() {
		// order is important here
		descriptionFactories = new ArrayList<>();
		// @ValueMapper annotations have preferences (since they can also bind all following types)
		descriptionFactories.add(new ForcedLeafBindingFactory());
		// collection and map before interface (since they are themselves interfaces)
		descriptionFactories.add(new ArrayBindingFactory());
		descriptionFactories.add(new CollectionBindingFactory());
		descriptionFactories.add(new MapBindingFactory());
		descriptionFactories.add(new InterfaceBindingFactory());
		descriptionFactories.add(new LeafBindingFactory());
	}

	@SuppressWarnings("unchecked")
	public ConfigBinding<?> toRootConfigBinding(Type forType, BindingSettings bindingSettings) {
		return toConfigBinding(BindingType.newBindingType(forType, bindingSettings));
	}

	public ConfigBinding<?> toConfigBinding(BindingType bindingType) {
		return descriptionFactories.stream()
				.map(configDescriptionFactory -> configDescriptionFactory.maybeCreate(bindingType, this))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new ConfijException(
						"Unable to bind to type '{}'. This type cannot be handled by any of the factories. " +
								"Either replace this type definition or add a custom {} to {}.", bindingType.getResolvedType(),
						ConfigBindingFactory.class.getName(), ConfigBinder.class)); // FIXME: wrong text
	}
}
