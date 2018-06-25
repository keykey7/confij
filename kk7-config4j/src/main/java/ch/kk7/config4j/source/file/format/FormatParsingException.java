package ch.kk7.config4j.source.file.format;

import ch.kk7.config4j.source.Config4jSourceException;

public class FormatParsingException extends Config4jSourceException {
	private FormatParsingException(String s, Object... args) {
		super(s, args);
	}

	public static FormatParsingException invalidFormat(String format, String detail, Object... args) {
		return new FormatParsingException("unable to parse configuration as '" + format + "', " + detail, args);
	}
}
