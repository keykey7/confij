package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.file.format.PropertiesFormat;
import ch.kk7.confij.tree.ConfijNode;

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
		if (value == null) {
			properties.remove(key);
		} else {
			properties.setProperty(key, value);
		}
		return this;
	}

	@Override
	public void override(ConfijNode rootNode) {
		overrideWithProperties(rootNode, properties);
	}
}
