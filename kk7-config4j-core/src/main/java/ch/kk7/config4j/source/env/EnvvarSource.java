package ch.kk7.config4j.source.env;

import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.ConfigSourceBuilder;
import ch.kk7.config4j.source.file.format.PropertiesFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.net.URI;
import java.util.Optional;

public class EnvvarSource extends PropertiesFormat implements ConfigSource, ConfigSourceBuilder {
	public static final String SCHEME = "env";

	private Object deepMap;

	public EnvvarSource() {
		setSeparator("_");
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		if (deepMap == null) {
			// envvars don't change: we can cache them forever
			deepMap = flatToDeepWithPrefix(simpleConfig.getConfig(), System.getenv());
		}
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	@Override
	public Optional<EnvvarSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			EnvvarSource source = new EnvvarSource();
			source.setPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
