package ch.kk7.config4j.format;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The definition of how the config must look like.
 */
public abstract class ConfigFormat {
	private final FormatSettings formatSettings;

	protected ConfigFormat(FormatSettings formatSettings) {
		this.formatSettings = Objects.requireNonNull(formatSettings);
	}

	public FormatSettings getFormatSettings() {
		return formatSettings;
	}

	public static class ConfigFormatLeaf extends ConfigFormat {
		public ConfigFormatLeaf(FormatSettings formatSettings) {
			super(formatSettings);
		}
	}

	public static class ConfigFormatList extends ConfigFormat {
		private final ConfigFormat anyChild;

		public ConfigFormatList(FormatSettings formatSettings, ConfigFormat anyChild) {
			super(formatSettings);
			this.anyChild = Objects.requireNonNull(anyChild);
		}

		public ConfigFormat anyChild() {
			return anyChild;
		}
	}

	public static class ConfigFormatMap extends ConfigFormat {
		private final ConfigFormat anyChild;
		private final Map<String, ConfigFormat> children;

		private ConfigFormatMap(FormatSettings formatSettings, ConfigFormat anyChild, Map<String, ConfigFormat> children) {
			super(formatSettings);
			this.anyChild = anyChild;
			this.children = Collections.unmodifiableMap(children);
		}

		public static ConfigFormatMap fixedKeysMap(FormatSettings formatSettings, Map<String, ConfigFormat> children) {
			return new ConfigFormatMap(formatSettings, null, children);
		}

		public static ConfigFormatMap anyKeyMap(FormatSettings formatSettings, ConfigFormat anyChild) {
			return new ConfigFormatMap(formatSettings, Objects.requireNonNull(anyChild), Collections.emptyMap());
		}

		public Optional<ConfigFormat> anyChild() {
			return Optional.ofNullable(anyChild);
		}

		public Map<String, ConfigFormat> getChildren() {
			return children;
		}

		public Optional<ConfigFormat> get(String key) {
			if (children.containsKey(key)) {
				return Optional.of(children.get(key));
			}
			return anyChild();
		}
	}
}
