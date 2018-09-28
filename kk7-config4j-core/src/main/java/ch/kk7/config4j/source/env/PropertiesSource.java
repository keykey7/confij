package ch.kk7.config4j.source.env;

import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.file.format.PropertiesFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.util.Properties;

public class PropertiesSource extends PropertiesFormat implements ConfigSource {
	private final Properties properties;

	public PropertiesSource() {
		this(new Properties());
	}

	public PropertiesSource(Properties properties) {
		this.properties = properties;
		setSeparator(".");
	}

	public PropertiesSource with(String key, String value) {
		properties.setProperty(key, value);
		return this;
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		overrideWithProperties(simpleConfig, properties);
	}
}
