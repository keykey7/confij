package ch.kk7.confij.source.file.resource;

import ch.kk7.confij.source.Config4jSourceException;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

public class ResourceFetchingException extends Config4jSourceException {
	private ResourceFetchingException(String s, Object... args) {
		super(s, args);
	}

	public static ResourceFetchingException unableToFetch(String path, String detail, Object... args) {
		return new ResourceFetchingException("unable to read configuration from '" + path + "', " + detail, args);
	}

	public static ResourceFetchingException unsupported(URI path, Collection<ConfijResourceProvider> supportedResources) {
		return new ResourceFetchingException("cannot read from resource '" +
				path +
				"', as none of the resource handlers supports this: " +
				supportedResources.stream()
						.map(ConfijResourceProvider::getClass)
						.map(Class::getName)
						.collect(Collectors.joining(", ")));
	}
}
