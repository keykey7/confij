package ch.kk7.confij.validation;

import ch.kk7.confij.tree.ConfijNode;

@FunctionalInterface
public interface ConfijValidator {
	ConfijValidator NOOP = config -> {
	};

	default void validate(Object config, ConfijNode confijNode) {
		validate(config);
	}

	void validate(Object config) throws ConfijValidationException;
}
