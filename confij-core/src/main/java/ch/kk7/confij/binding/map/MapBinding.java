package ch.kk7.confij.binding.map;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.ConfijNode;

import java.util.Map;

public class MapBinding<T> implements ConfigBinding<Map<String, T>> {
	private final MapBuilder builder;
	private final ConfigBinding<T> componentDescription;

	public MapBinding(MapBuilder builder, BindingType bindingType, ConfigBinder configBinder) {
		this.builder = builder;
		//noinspection unchecked
		componentDescription = (ConfigBinding<T>) configBinder.toConfigBinding(bindingType);
	}

	@Override
	public NodeDefinitionMap describe(NodeBindingContext nodeBindingContext) {
		return NodeDefinitionMap.anyKeyMap(nodeBindingContext, componentDescription.describe(nodeBindingContext));
	}

	@Override
	public Map<String, T> bind(ConfijNode config) {
		Map<String, T> map = builder.newInstance();
		config.getChildren().forEach((key,childConfig) -> map.put(key, componentDescription.bind(childConfig)));
		return builder.tryHarden(map);
	}
}
