package ch.kk7.confij.binding;

import ch.kk7.confij.common.Config4jException;

public class BindingException extends Config4jException {
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
