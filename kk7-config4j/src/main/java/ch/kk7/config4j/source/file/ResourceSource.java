package ch.kk7.config4j.source.file;

import ch.kk7.config4j.format.resolve.IVariableResolver;
import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.file.resource.Config4jResource;
import ch.kk7.config4j.source.file.format.ResourceFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.util.Objects;

public class ResourceSource implements ConfigSource {
	private final String pathTemplate;
	private final Config4jResource resource;
	private final ResourceFormat format;
	private final IVariableResolver resolver;

	public ResourceSource(String pathTemplate, Config4jResource resource, ResourceFormat format, IVariableResolver resolver) {
		this.pathTemplate = Objects.requireNonNull(pathTemplate);
		this.resource = Objects.requireNonNull(resource);
		this.format = Objects.requireNonNull(format);
		this.resolver = resolver;
	}

	private IVariableResolver getResolver(SimpleConfig simpleConfig) {
		if (resolver != null) {
			return resolver;
		}
		return simpleConfig.getConfig()
				.getFormatSettings()
				.getVariableResolver();
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		String actualPath = getResolver(simpleConfig).resolve(simpleConfig, pathTemplate);
		String configAsStr = resource.read(actualPath);
		format.override(simpleConfig, configAsStr);
	}
}
