package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfigSourceBuilder;

import java.net.URI;
import java.util.Optional;

public class SystemPropertiesSource extends PropertiesSource implements ConfigSourceBuilder {
	public static final String SCHEME = "sys";

	public SystemPropertiesSource() {
		super(System.getProperties());
	}

	@Override
	public Optional<SystemPropertiesSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			SystemPropertiesSource source = new SystemPropertiesSource();
			source.setGlobalPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
