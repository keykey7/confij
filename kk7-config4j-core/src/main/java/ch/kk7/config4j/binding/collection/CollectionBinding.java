package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBinding.BindResult.BindResultBuilder;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigList;

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
	public BindResult<Collection<T>> bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigList)) {
			throw new IllegalStateException("expected a config list, but got: " + config);
		}
		BindResultBuilder<Collection<T>> resultBuilder = BindResult.builder();
		List<SimpleConfig> configList = ((SimpleConfigList) config).list();
		Collection<T> collection = builder.newInstance();

		for (int i = 0; i < configList.size(); i++) {
			BindResult<T> itemBindResult = componentDescription.bind(configList.get(i));
			collection.add(itemBindResult.getValue());
			resultBuilder.sibling(String.valueOf(i), itemBindResult);
		}
		return resultBuilder.value(builder.tryHarden(collection))
				.build();
	}
}
