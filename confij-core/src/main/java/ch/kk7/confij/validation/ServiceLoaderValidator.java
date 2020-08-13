package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;

import java.util.List;

import static ch.kk7.confij.common.ServiceLoaderUtil.instancesOf;

// NOT detectable by serviceLoader himself
public class ServiceLoaderValidator<T> implements ConfijValidator<T> {
	private final List<ConfijValidator> validators;

	public ServiceLoaderValidator() {
		validators = instancesOf(ConfijValidator.class);
	}

	@Override
	public void validate(BindingResult<T> bindingResult) {
		validators.forEach(validator -> validator.validate(bindingResult));
	}
}
