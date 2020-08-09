package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.format.PropertiesFormat;
import ch.kk7.confij.tree.ConfijNode;

import java.util.Properties;

public class PropertiesSource extends PropertiesFormat implements ConfijSource {
	private final Properties properties;

	public PropertiesSource() {
		this(new Properties());
	}

	public PropertiesSource(Properties properties) {
		this.properties = properties;
		setSeparator(".");
	}

	public static PropertiesSource of(String key, String value) {
		return new PropertiesSource().with(key, value);
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
