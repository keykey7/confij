package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;

@FunctionalInterface
public interface ConfijValidator<T> {
	ConfijValidator<?> NOOP = config -> {
	};

	void validate(BindingResult<T> bindingResult) throws ConfijValidationException;
}
