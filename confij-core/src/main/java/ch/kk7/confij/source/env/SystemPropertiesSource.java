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
public class SystemPropertiesSource extends PropertiesFormat implements ConfijSource {
	@Override
	public void override(ConfijNode rootNode) {
		overrideWithProperties(rootNode, System.getProperties());
	}

	@ToString
	@AutoService(ConfijAnySource.class)
	public static class SystemPropertiesAnySource implements ConfijAnySource {
		public static final String SCHEME = "sys";

		@Override
		public Optional<ConfijSource> fromURI(String pathTemplate) {
			return Util.getScheme(pathTemplate)
					.filter(scheme -> scheme.equals(SCHEME))
					.map(__ -> {
						String path = Util.getSchemeSpecificPart(pathTemplate);
						SystemPropertiesSource source = new SystemPropertiesSource();
						source.setGlobalPrefix(path);
						return source;
					});
		}
	}
}
