package ch.kk7.config4j.source;

import ch.kk7.config4j.format.resolve.IVariableResolver;
import ch.kk7.config4j.source.env.EnvvarSource;
import ch.kk7.config4j.source.env.SystemPropertiesSource;
import ch.kk7.config4j.source.file.AnyResourceBuilder;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AnySource implements ConfigSource {
	private final List<ConfigSourceBuilder> sourceBuilders;
	private final String pathTemplate;
	private IVariableResolver resolverOverride;

	private IVariableResolver getResolver(SimpleConfig simpleConfig) {
		if (resolverOverride != null) {
			return resolverOverride;
		}
		return simpleConfig.getConfig()
				.getFormatSettings()
				.getVariableResolver();
	}

	public AnySource(String pathTemplate) {
		this.pathTemplate = Objects.requireNonNull(pathTemplate);
		sourceBuilders = new ArrayList<>(Arrays.asList(new EnvvarSource(), new SystemPropertiesSource(), new AnyResourceBuilder()));
	}

	public AnySource setResolver(IVariableResolver resolver) {
		resolverOverride = resolver;
		return this;
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		String actualPath = getResolver(simpleConfig).resolve(simpleConfig, pathTemplate);
		URI path = URI.create(actualPath);
		sourceBuilders.stream()
				.map(sb -> sb.fromURI(path))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new Config4jSourceException("cannot handle path {} (resolved from {})", path, pathTemplate))
				.override(simpleConfig);
	}
}
