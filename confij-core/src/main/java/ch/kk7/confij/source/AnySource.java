package ch.kk7.confij.source;

import ch.kk7.confij.format.resolve.IVariableResolver;
import ch.kk7.confij.source.env.EnvvarSource;
import ch.kk7.confij.source.env.SystemPropertiesSource;
import ch.kk7.confij.source.file.AnyResourceBuilder;
import ch.kk7.confij.source.simple.ConfijNode;

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

	private IVariableResolver getResolver(ConfijNode node) {
		if (resolverOverride != null) {
			return resolverOverride;
		}
		return node.getConfig()
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
	public void override(ConfijNode simpleConfig) {
		String actualPath = getResolver(simpleConfig).resolve(simpleConfig, pathTemplate);
		URI path = URI.create(actualPath);
		sourceBuilders.stream()
				.map(sb -> sb.fromURI(path))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> {
					String addon = pathTemplate.equals(actualPath) ? "" : " (resolved from '" + pathTemplate + "')";
					return new Config4jSourceException("failed to load source data from '{}'{}", path, addon);
				})
				.override(simpleConfig);
	}
}
