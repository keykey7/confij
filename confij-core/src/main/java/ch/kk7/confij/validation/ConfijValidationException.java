package ch.kk7.confij.validation;

import ch.kk7.confij.common.ConfijException;

public class ConfijValidationException extends ConfijException {
	public ConfijValidationException(String s) {
		super(s);
	}

	public ConfijValidationException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ConfijValidationException(String s, Object... args) {
		super(s, args);
	}
}
