package ch.kk7.confij.source.format;

import ch.kk7.confij.source.ConfijSourceException;

/**
 * @deprecated to be replaced with ConfijSourceException
 */
@Deprecated
public class ConfijSourceFormatException extends ConfijSourceException {
	public ConfijSourceFormatException(String s, Object... args) {
		super(s, args);
	}

	public static ConfijSourceFormatException invalidFormat(String format, String detail, Object... args) {
		return new ConfijSourceFormatException("unable to parse configuration as '" + format + "', " + detail, args);
	}
}
