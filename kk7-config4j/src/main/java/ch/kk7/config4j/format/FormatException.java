package ch.kk7.config4j.format;

import ch.kk7.config4j.common.Config4jException;

public class FormatException extends Config4jException {
	public FormatException(String s, Object... args) {
		super(s, args);
	}
}
