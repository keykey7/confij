package ch.kk7.confij.source.resource;

import ch.kk7.confij.source.ConfijSourceException;

public class ConfijSourceFetchingException extends ConfijSourceException {
	public ConfijSourceFetchingException(String s, Object... args) {
		super(s, args);
	}

	public static ConfijSourceFetchingException unableToFetch(String path, String detail, Object... args) {
		return new ConfijSourceFetchingException("unable to read configuration from '{}', " + detail, path, args);
	}
}
