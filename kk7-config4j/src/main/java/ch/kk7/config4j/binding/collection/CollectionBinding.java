package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigList;
import com.fasterxml.classmate.ResolvedType;

import java.util.Collection;
import java.util.List;

public class CollectionBinding<T> implements ConfigBinding<Collection<T>> {
	private final Collection<T> set;
	private final Collection<T> publicSet;
	private final ConfigBinding<T> componentDescription;

	public CollectionBinding(UnmodifiableCollectionBuilder<T, Collection<T>> builder, ResolvedType componentType,
			ConfigBinder configBinder) {
		set = builder.getModifyableInstance();
		publicSet = builder.harden(set);
		//noinspection unchecked
		componentDescription = (ConfigBinding<T>) configBinder.toConfigBinding(componentType);
	}

	@Override
	public ConfigFormatList describe(FormatSettings formatSettings) {
		return new ConfigFormatList(formatSettings, componentDescription.describe(formatSettings));
	}

	@Override
	public Collection<T> bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigList)) {
			throw new IllegalStateException("expected a config list, but got: " + config);
		}
		List<SimpleConfig> configList = ((SimpleConfigList) config).list();
		set.clear();
		for (SimpleConfig configItem : configList) {
			T listItem = componentDescription.bind(configItem);
			set.add(listItem);
		}
		return publicSet;
	}
}
