package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.format.PropertiesFormat;
import ch.kk7.confij.tree.ConfijNode;
import lombok.ToString;

import java.util.Properties;

@ToString
public class ExplicitPropertiesSource extends PropertiesFormat implements ConfijSource {
	private final Properties properties;

	public ExplicitPropertiesSource() {
		this(new Properties());
	}

	public ExplicitPropertiesSource(Properties properties) {
		this.properties = properties;
	}

	public static ExplicitPropertiesSource of(String key, String value) {
		return new ExplicitPropertiesSource().set(key, value);
	}

	public ExplicitPropertiesSource set(String key, String value) {
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
