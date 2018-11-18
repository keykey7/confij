package ch.kk7.confij.binding;

import ch.kk7.confij.common.ConfijException;

public class BindingException extends ConfijException {
	public BindingException(String s) {
		super(s);
	}

	public BindingException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public BindingException(String s, Object... args) {
		super(s, args);
	}
}
