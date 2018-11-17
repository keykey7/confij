package ch.kk7.confij.binding.map;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;

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
	public ConfigFormatMap describe(FormatSettings formatSettings) {
		return ConfigFormatMap.anyKeyMap(formatSettings, componentDescription.describe(formatSettings));
	}

	@Override
	public Map<String, T> bind(ConfijNode config) {
		Map<String, T> map = builder.newInstance();
		config.getChildren().forEach((key,childConfig) -> map.put(key, componentDescription.bind(childConfig)));
		return builder.tryHarden(map);
	}
}
