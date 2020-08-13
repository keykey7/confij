package ch.kk7.confij.binding;

import ch.kk7.confij.binding.array.ArrayBindingFactory;
import ch.kk7.confij.binding.collection.CollectionBindingFactory;
import ch.kk7.confij.binding.intf.InterfaceBindingFactory;
import ch.kk7.confij.binding.leaf.ForcedLeafBindingFactory;
import ch.kk7.confij.binding.leaf.LeafBindingFactory;
import ch.kk7.confij.binding.map.MapBindingFactory;
import ch.kk7.confij.logging.ConfijLogger;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ToString
public class ConfigBinder {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(ConfigBinder.class);
	@Getter
	private List<ConfigBindingFactory<?>> bindingFactories;

	public ConfigBinder() {
		// order is important here
		bindingFactories = new ArrayList<>();
		// @ValueMapper annotations have preferences (since they can also bind all following types)
		bindingFactories.add(new ForcedLeafBindingFactory());
		bindingFactories.add(new LeafBindingFactory());
		// collection and map before interface (since they are themselves interfaces)
		bindingFactories.add(new ArrayBindingFactory());
		bindingFactories.add(new CollectionBindingFactory());
		bindingFactories.add(new MapBindingFactory());
		// interface binding last (since everything before is an interface, too)
		bindingFactories.add(new InterfaceBindingFactory());
	}

	@SuppressWarnings("unchecked")
	public <T> ConfigBinding<T> toRootConfigBinding(Class<T> forClass) {
		return (ConfigBinding<T>) toRootConfigBinding(forClass, BindingContext.newDefaultContext());
	}

	public ConfigBinding<?> toRootConfigBinding(Type forType, BindingContext bindingContext) {
		return toConfigBinding(BindingType.newBindingType(forType, bindingContext));
	}

	public ConfigBinding<?> toConfigBinding(BindingType bindingType) {
		return bindingFactories.stream()
				.map(configDescriptionFactory -> configDescriptionFactory.maybeCreate(bindingType, this))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> {
					LOGGER.info("Type {} could not be handled by any of the configured bindingFactories: {}", bindingType,
							bindingFactories);
					return new ConfijDefinitionException(
							"Unable to bind to type '{}'. This type cannot be handled by any of the binding-factories. " +
									"Either replace this type definition or add a custom {} to {}. " +
									"Most commonly you want to bind a leaf-type (and not a container for properties), " +
									"where it is most simple to register a ValueMapping in the ConfijBuilder or add a " +
									"custom @ValueMapping annotation to this type.", bindingType.getResolvedType(),
							ConfigBindingFactory.class.getName(), ConfigBinder.class);
				});
	}
}
