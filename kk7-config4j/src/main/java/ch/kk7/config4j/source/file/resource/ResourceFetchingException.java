package ch.kk7.config4j.source.file.resource;

import ch.kk7.config4j.source.Config4jSourceException;

public class ResourceFetchingException extends Config4jSourceException {
	private ResourceFetchingException(String s, Object... args) {
		super(s, args);
	}

	public static ResourceFetchingException unableToFetch(String path, String detail, Object... args) {
		return new ResourceFetchingException("unable to read configuration from '" + path + "', " + detail, args);
	}
}
