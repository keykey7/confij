package ch.kk7.config4j.source;

import ch.kk7.config4j.source.env.EnvvarSource;
import ch.kk7.config4j.source.env.SystemPropertiesSource;
import ch.kk7.config4j.source.file.ResourceSource;

import java.net.URI;

public class SourceBuilder {
	// TODO: all sources should support
	// removePrefix("a.prefix.to.remove")
	// addPrefix("subcomponent")

	private SourceBuilder() {

	}

	public static ConfigSource of(String str) {
		URI uri = URI.create(str);
		String scheme = uri.getScheme();
		String path = uri.getSchemeSpecificPart();
		if (EnvvarSource.SCHEME.equals(scheme)) {
			EnvvarSource configSource = new EnvvarSource();
			// TODO: think again on what relative vs absolute URI means in this context:
			configSource.setPrefix(path);
			return configSource;
		}
		if (SystemPropertiesSource.SCHEME.equals(scheme)) {
			SystemPropertiesSource configSource = new SystemPropertiesSource();
			// TODO: think again on what relative vs absolute URI means in this context:
			configSource.setPrefix(path);
			return configSource;
		}
		return new ResourceSource(str);
	}
}
