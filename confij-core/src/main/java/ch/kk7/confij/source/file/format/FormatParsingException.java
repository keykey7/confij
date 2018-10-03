package ch.kk7.confij.source.file.format;

import ch.kk7.confij.source.Config4jSourceException;

public class FormatParsingException extends Config4jSourceException {
	private FormatParsingException(String s, Object... args) {
		super(s, args);
	}

	public static FormatParsingException unknownFormat(String guess) {
		return new FormatParsingException("unable to guess the format based on '" + guess + "'");
	}

	public static FormatParsingException invalidFormat(String format, String detail, Object... args) {
		return new FormatParsingException("unable to parse configuration as '" + format + "', " + detail, args);
	}
}
