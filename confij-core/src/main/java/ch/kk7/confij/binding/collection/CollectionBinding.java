package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatList;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;

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
	public ConfigFormatList describe(FormatSettings formatSettings) {
		return new ConfigFormatList(formatSettings, componentDescription.describe(formatSettings));
	}

	@Override
	public Collection<T> bind(ConfijNode config) {
		// we use a map here to allow for sparse array implementations
		// TODO: add config to enforce continuous indexes
		List<ConfijNode> childNodes = CollectionUtil.childrenAsContinuousList(config);
		Collection<T> collection = builder.newInstance();
		for (ConfijNode childNode : childNodes) {
			T listItem = componentDescription.bind(childNode);
			collection.add(listItem);
		}
		return builder.tryHarden(collection);
	}
}
