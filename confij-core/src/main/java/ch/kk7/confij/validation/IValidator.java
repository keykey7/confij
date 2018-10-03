package ch.kk7.confij.validation;

@FunctionalInterface
public interface IValidator {
	IValidator NOOP = config -> {
	};

	void validate(Object config);
}
