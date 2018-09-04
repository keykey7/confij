package ch.kk7.config4j.source.env;

import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.ConfigSourceBuilder;
import ch.kk7.config4j.source.file.format.PropertiesFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.net.URI;
import java.util.Optional;

public class SystemPropertiesSource extends PropertiesFormat implements ConfigSource, ConfigSourceBuilder {
	public static final String SCHEME = "sys";

	public SystemPropertiesSource() {
		setSeparator(".");
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		overrideWithProperties(simpleConfig, System.getProperties());
	}

	@Override
	public Optional<SystemPropertiesSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			SystemPropertiesSource source = new SystemPropertiesSource();
			source.setPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
