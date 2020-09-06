package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.common.ServiceLoaderUtil;

import java.util.List;

// NOT detectable by serviceLoader himself
public class ServiceLoaderValidator<T> implements ConfijValidator<T> {
	private final List<ConfijValidator> validators;

	public ServiceLoaderValidator() {
		validators = ServiceLoaderUtil.instancesOf(ConfijValidator.class);
	}

	@Override
	public void validate(BindingResult<T> bindingResult) {
		validators.forEach(validator -> validator.validate(bindingResult));
	}
}
