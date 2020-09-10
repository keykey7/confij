package ch.kk7.confij.source.env;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.any.ConfijAnySource;
import ch.kk7.confij.source.format.PropertiesFormat;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.ToString;

import java.util.Optional;

@ToString
public class EnvvarSource extends PropertiesFormat implements ConfijSource {
	private Object deepMap;

	protected EnvvarSource(String prefix) {
		super("_", prefix);
	}

	public static EnvvarSource withPrefix(String prefix) {
		return new EnvvarSource(prefix);
	}

	@Override
	public void override(ConfijNode rootNode) {
		if (deepMap == null) {
			// envvars don't change: we can cache them forever
			deepMap = flatToNestedMapWithPrefix(System.getenv());
		}
		overrideWithDeepMap(rootNode, deepMap);
	}

	@ToString
	@AutoService(ConfijAnySource.class)
	public static class EnvvarAnySource implements ConfijAnySource {
		public static final String SCHEME = "env";

		@Override
		public Optional<ConfijSource> fromURI(String pathTemplate) {
			return Util.getScheme(pathTemplate)
					.filter(scheme -> scheme.equals(SCHEME))
					.map(scheme -> {
						String path = Util.getSchemeSpecificPart(pathTemplate);
						return EnvvarSource.withPrefix(path);
					});
		}
	}
}
