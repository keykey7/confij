package ch.kk7.confij.format;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The definition of how the config must look like.
 */
@Value
@NonFinal
public abstract class ConfigFormat {
	@NonNull
	private final FormatSettings formatSettings;

	public boolean isValueHolder() {
		return false;
	}

	@NonNull
	public abstract ConfigFormat formatForChild(String configKey);

	@NonNull
	public Set<String> getMandatoryKeys() {
		return Collections.emptySet();
	}

	@ToString
	public static class ConfigFormatLeaf extends ConfigFormat {
		public ConfigFormatLeaf(FormatSettings formatSettings) {
			super(formatSettings);
		}

		@NonNull
		@Override
		public ConfigFormat formatForChild(String configKey) {
			throw new FormatException("a leaf isn't allowed to have children, not even for '{}'", configKey);
		}

		@Override
		public boolean isValueHolder() {
			return true;
		}
	}

	@ToString
	public static class ConfigFormatList extends ConfigFormat {
		private final ConfigFormat anyChild;

		public ConfigFormatList(FormatSettings formatSettings, ConfigFormat anyChild) {
			super(formatSettings);
			this.anyChild = Objects.requireNonNull(anyChild);
		}

		@NonNull
		@Override
		public ConfigFormat formatForChild(String configKey) {
			final int index;
			try {
				index = Integer.parseInt(configKey);
			} catch (NumberFormatException e) {
				throw new FormatException("invalid config key, expected a number, but found {}", configKey, e);
			}
			if (index < 0) {
				throw new FormatException("invalid config key, expected a stictly positive number, but found {}", index);
			}
			return anyChild;
		}
	}

	@ToString
	public static class ConfigFormatMap extends ConfigFormat {
		private final ConfigFormat anyChild;
		private final Map<String, ConfigFormat> children;

		private ConfigFormatMap(FormatSettings formatSettings, ConfigFormat anyChild, Map<String, ConfigFormat> children) {
			super(formatSettings);
			this.anyChild = anyChild;
			if (children.containsValue(null)) {
				throw new IllegalArgumentException("invalid null value in " + children);
			}
			this.children = Collections.unmodifiableMap(children);
		}

		public static ConfigFormatMap fixedKeysMap(FormatSettings formatSettings, Map<String, ConfigFormat> children) {
			return new ConfigFormatMap(formatSettings, null, children);
		}

		public static ConfigFormatMap anyKeyMap(FormatSettings formatSettings, ConfigFormat anyChild) {
			return new ConfigFormatMap(formatSettings, Objects.requireNonNull(anyChild), Collections.emptyMap());
		}

		@NonNull
		@Override
		public ConfigFormat formatForChild(String configKey) {
			if (children.containsKey(configKey)) {
				return children.get(configKey);
			}
			if (anyChild != null) {
				return anyChild;
			}
			throw new FormatException("map-like format doesn't allow key '{}', allowed are: {}", configKey, children.keySet());
		}

		@NonNull
		@Override
		public Set<String> getMandatoryKeys() {
			return children.keySet();
		}
	}
}
