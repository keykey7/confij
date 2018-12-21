package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceBuilder;
import com.google.auto.service.AutoService;

import java.net.URI;
import java.util.Optional;

@AutoService(ConfijSourceBuilder.class)
public class SystemPropertiesSource extends PropertiesSource implements ConfijSourceBuilder {
	public static final String SCHEME = "sys";

	public SystemPropertiesSource() {
		super(System.getProperties());
	}

	@Override
	public Optional<ConfijSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			SystemPropertiesSource source = new SystemPropertiesSource();
			source.setGlobalPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
