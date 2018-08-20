package ch.kk7.config4j.source.file;

import ch.kk7.config4j.format.resolve.IVariableResolver;
import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.file.format.FormatBuilder;
import ch.kk7.config4j.source.file.format.ResourceFormat;
import ch.kk7.config4j.source.file.resource.Config4jResource;
import ch.kk7.config4j.source.file.resource.ResourceBuilder;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.net.URI;
import java.util.Objects;

public class ResourceSource implements ConfigSource {
	private final String pathTemplate;
	private final Config4jResource resourceOverride;
	private final ResourceFormat formatOverride;
	private final IVariableResolver resolverOverride;

	public ResourceSource(String pathTemplate) {
		this(pathTemplate, null, null, null);
	}

	public ResourceSource(String pathTemplate, Config4jResource resourceOverride, ResourceFormat formatOverride,
			IVariableResolver resolverOverride) {
		this.pathTemplate = Objects.requireNonNull(pathTemplate);
		this.resourceOverride = resourceOverride;
		this.formatOverride = formatOverride;
		this.resolverOverride = resolverOverride;

		// TODO: resolve format and resource if the pathTemplate doesn't contain any placeholders
	}

	private Config4jResource getResource(URI actualUri) {
		if (resourceOverride != null) {
			return resourceOverride;
		}
		return ResourceBuilder.forPath(actualUri);
	}

	private ResourceFormat getFormat(String actualPath) {
		if (formatOverride != null) {
			return formatOverride;
		}
		return FormatBuilder.forPath(actualPath);
	}

	private IVariableResolver getResolver(SimpleConfig simpleConfig) {
		if (resolverOverride != null) {
			return resolverOverride;
		}
		return simpleConfig.getConfig()
				.getFormatSettings()
				.getVariableResolver();
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		String actualPath = getResolver(simpleConfig).resolve(simpleConfig, pathTemplate);
		URI actualUri = URI.create(actualPath);
		String configAsStr = getResource(actualUri).read(actualUri.getSchemeSpecificPart());
		getFormat(actualPath).override(simpleConfig, configAsStr);
	}
}
