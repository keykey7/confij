package ch.kk7.confij.binding.map;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;

import java.util.ArrayList;
import java.util.List;
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
	public BindingResult<Map<String, T>> bind(ConfijNode config) {
		List<BindingResult<?>> bindingResultChildren = new ArrayList<>();
		Map<String, T> map = builder.newInstance();
		config.getChildren().forEach((key,childConfig) -> {
			BindingResult<T> childValue = componentDescription.bind(childConfig);
			map.put(key, childValue.getValue());
			bindingResultChildren.add(childValue);
		});
		Map<String, T> hardenedMap = builder.tryHarden(map);
		return BindingResult.of(hardenedMap, config, bindingResultChildren);
	}
}
