package ch.kk7.confij.source.env;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.format.PropertiesFormat;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;

import java.net.URI;
import java.util.Optional;

@AutoService(ConfijSourceBuilder.class)
public class EnvvarSource extends PropertiesFormat implements ConfijSource, ConfijSourceBuilder {
	public static final String SCHEME = "env";

	private Object deepMap;

	public EnvvarSource() {
		setSeparator("_");
	}

	@Override
	public void override(ConfijNode rootNode) {
		if (deepMap == null) {
			// envvars don't change: we can cache them forever
			deepMap = flatToNestedMapWithPrefix(System.getenv());
		}
		overrideWithDeepMap(rootNode, deepMap);
	}

	@Override
	public Optional<ConfijSource> fromURI(URI path) {
		if (SCHEME.equals(path.getScheme())) {
			EnvvarSource source = new EnvvarSource();
			source.setGlobalPrefix(path.getSchemeSpecificPart());
			return Optional.of(source);
		}
		return Optional.empty();
	}
}
