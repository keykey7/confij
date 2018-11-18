package ch.kk7.confij.format;

import ch.kk7.confij.common.ConfijException;

public class FormatException extends ConfijException {
	public FormatException(String s, Object... args) {
		super(s, args);
	}
}
