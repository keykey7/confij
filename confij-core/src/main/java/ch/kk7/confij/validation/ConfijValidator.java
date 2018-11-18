package ch.kk7.confij.validation;

@FunctionalInterface
public interface ConfijValidator {
	ConfijValidator NOOP = config -> {
	};

	void validate(Object config);
}
