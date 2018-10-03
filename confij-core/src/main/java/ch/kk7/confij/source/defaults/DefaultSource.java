package ch.kk7.confij.source.defaults;

import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatList;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.simple.SimpleConfig;
import ch.kk7.confij.source.simple.SimpleConfigLeaf;
import ch.kk7.confij.source.simple.SimpleConfigList;
import ch.kk7.confij.source.simple.SimpleConfigMap;

import java.util.Objects;
import java.util.Set;

/**
 * This is a special ConfigSource since it does not simply override existing values.
 * It rather extends them, by introducing missing nodes and setting default values.
 */
public class DefaultSource implements ConfigSource {
	@Override
	public void override(SimpleConfig simpleConfig) {
		Objects.requireNonNull(simpleConfig);
		if (simpleConfig instanceof SimpleConfigLeaf) {
			override((SimpleConfigLeaf) simpleConfig);
		} else if (simpleConfig instanceof SimpleConfigList) {
			override((SimpleConfigList) simpleConfig);
		} else if (simpleConfig instanceof SimpleConfigMap) {
			override((SimpleConfigMap) simpleConfig);
		} else {
			throw new IllegalStateException("unknown SimpleConfig: " + simpleConfig);
		}
	}

	protected void override(SimpleConfigLeaf configLeaf) {
		String currentValue = configLeaf.get();
		if (currentValue == null) {
			configLeaf.set(configLeaf.getConfig()
					.getFormatSettings()
					.getDefaultValue()); // might still be null, though
		}
	}

	protected void override(SimpleConfigList configList) {
		configList.list()
				.forEach(this::override);
	}

	protected void override(SimpleConfigMap configMap) {
		Set<String> existingKeys = configMap.map()
				.keySet();
		configMap.map()
				.values()
				.forEach(this::override);

		// additionally to the existing values we have to add mandatory
		// keys which are not yet present
		configMap.getConfig()
				.getChildren()
				.entrySet()
				.stream()
				.filter(e -> !existingKeys.contains(e.getKey()))
				.forEach(e -> {
					String k = e.getKey();
					ConfigFormat configFormat = e.getValue();
					if (configFormat instanceof ConfigFormatLeaf) {
						override(configMap.putLeaf(k));
					} else if (configFormat instanceof ConfigFormatList) {
						override(configMap.putList(k));
					} else if (configFormat instanceof ConfigFormatMap) {
						override(configMap.putMap(k));
					} else {
						throw new IllegalStateException("unknown ConfigFormat: " + configFormat);
					}
				});
	}
}
