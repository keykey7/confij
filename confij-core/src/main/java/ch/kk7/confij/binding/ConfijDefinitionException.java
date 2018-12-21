package ch.kk7.confij.binding;

import ch.kk7.confij.common.ConfijException;

/**
 * Exception is thrown whenever there is an issue with the interface defining the configuration.
 * It is unrelated to any configuration data. The issue must be fixed in code.
 */
public class ConfijDefinitionException extends ConfijException {
	public ConfijDefinitionException(String s, Object... args) {
		super(s, args);
	}
}
