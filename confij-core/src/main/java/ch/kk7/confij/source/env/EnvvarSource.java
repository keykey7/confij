package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.ConfigSourceBuilder;
import ch.kk7.confij.source.file.format.PropertiesFormat;
import ch.kk7.confij.source.simple.ConfijNode;

import java.net.URI;
import java.util.Optional;

public class EnvvarSource extends PropertiesFormat implements ConfigSource, ConfigSourceBuilder {
	public static final String SCHEME = "env";

	private Object deepMap;

	public EnvvarSource() {
		setSeparator("_");
	}

	@Override
	public void override(ConfijNode simpleConfig) {
		if (deepMap == null) {
			// envvars don't change: we can cache them forever
			deepMap = flatToNestedMapWithPrefix(simpleConfig.getConfig(), System.getenv());
		}
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	@Override
	public Optional<EnvvarSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			EnvvarSource source = new EnvvarSource();
			source.setGlobalPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
