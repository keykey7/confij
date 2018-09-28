package ch.kk7.config4j.validation;

@FunctionalInterface
public interface IValidator {
	IValidator NOOP = config -> {
	};

	void validate(Object config);
}
