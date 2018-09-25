package ch.kk7.config4j.source.file;

import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.file.format.ResourceFormat;
import ch.kk7.config4j.source.file.resource.Config4jResource;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.net.URI;
import java.util.Objects;

public class FixedResourceSource implements ConfigSource {
	private final URI path;
	private final Config4jResource resource;
	private final ResourceFormat format;

	public FixedResourceSource(URI path, Config4jResource resource, ResourceFormat format) {
		this.path = Objects.requireNonNull(path);
		this.resource = Objects.requireNonNull(resource);
		this.format = Objects.requireNonNull(format);
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		String configAsStr = resource.read(path);
		format.override(simpleConfig, configAsStr);
	}
}