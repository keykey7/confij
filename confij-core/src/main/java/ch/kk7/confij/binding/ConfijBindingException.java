package ch.kk7.confij.binding;

import ch.kk7.confij.common.ConfijException;

/**
 * An issue when attempting to combine a configuration definition with actual configuration data.
 * The issue is likely resolvable by changing the config input (not code).
 */
public class ConfijBindingException extends ConfijException {
	public ConfijBindingException(String s) {
		super(s);
	}

	public ConfijBindingException(String s, Object... args) {
		super(s, args);
	}
}
