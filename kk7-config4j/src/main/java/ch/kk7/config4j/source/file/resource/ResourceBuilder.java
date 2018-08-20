package ch.kk7.config4j.source.file.resource;

import java.net.URI;
import java.util.Optional;

public class ResourceBuilder {
	private static final String DEFAULT_SCHEME = FileResource.SCHEME;

	private ResourceBuilder() {

	}

	public static Config4jResource forPath(URI uri) {
		String scheme = Optional.ofNullable(uri.getScheme())
				.orElse(DEFAULT_SCHEME);
		switch (scheme) {
			case FileResource.SCHEME:
				return new FileResource();
			case ClasspathResource.SCHEME:
				return new ClasspathResource();
			default:
				return new URLResource();
		}
	}
}
