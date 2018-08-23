package ch.kk7.config4j.binding;

import ch.kk7.config4j.common.Config4jException;

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
