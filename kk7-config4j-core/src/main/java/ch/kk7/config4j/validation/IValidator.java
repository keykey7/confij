package ch.kk7.config4j.validation;

import ch.kk7.config4j.binding.ConfigBinding.BindResult;

@FunctionalInterface
public interface IValidator {
	IValidator NOOP = config -> {
	};

	void validate(BindResult<?> config);
}
