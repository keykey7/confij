package ch.kk7.confij.format;

import ch.kk7.confij.common.Config4jException;

public class FormatException extends Config4jException {
	public FormatException(String s, Object... args) {
		super(s, args);
	}
}
