package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionBinding<T> implements ConfigBinding<Collection<T>> {
	private final CollectionBuilder builder;
	private final ConfigBinding<T> componentDescription;

	public CollectionBinding(CollectionBuilder builder, BindingType componentBindingType, ConfigBinder configBinder) {
		this.builder = builder;
		//noinspection unchecked
		componentDescription = (ConfigBinding<T>) configBinder.toConfigBinding(componentBindingType);
	}

	@Override
	public NodeDefinitionList describe(NodeBindingContext nodeBindingContext) {
		return new NodeDefinitionList(nodeBindingContext, componentDescription.describe(nodeBindingContext));
	}

	@Override
	public BindingResult<Collection<T>> bind(ConfijNode config) {
		List<BindingResult<?>> bindingResultChildren = new ArrayList<>();
		// TODO: add config to enforce continuous indexes as we currently allow sparse collection types
		List<ConfijNode> childNodes = CollectionUtil.childrenAsContinuousList(config);
		Collection<T> collection = builder.newInstance();
		for (ConfijNode childNode : childNodes) {
			BindingResult<T> listItem = componentDescription.bind(childNode);
			collection.add(listItem.getValue());
			bindingResultChildren.add(listItem);
		}
		return BindingResult.of(builder.tryHarden(collection), config, bindingResultChildren);
	}
}
