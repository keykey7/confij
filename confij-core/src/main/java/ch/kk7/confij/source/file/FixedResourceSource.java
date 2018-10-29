package ch.kk7.confij.source.file;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.file.format.ResourceFormat;
import ch.kk7.confij.source.file.resource.Config4jResource;
import ch.kk7.confij.source.simple.ConfijNode;

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
	public void override(ConfijNode simpleConfig) {
		String configAsStr = resource.read(path);
		format.override(simpleConfig, configAsStr);
	}
}
