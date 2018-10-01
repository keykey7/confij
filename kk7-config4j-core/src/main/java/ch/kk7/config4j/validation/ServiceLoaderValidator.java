package ch.kk7.config4j.validation;

import ch.kk7.config4j.binding.ConfigBinding.BindResult;

import java.util.List;

import static ch.kk7.config4j.common.Util.serviceLoaderOf;

// NOT detectable by serviceLoader himself
public class ServiceLoaderValidator implements IValidator {
	private final List<IValidator> validators;

	public ServiceLoaderValidator() {
		validators = serviceLoaderOf(IValidator.class);
	}

	@Override
	public void validate(BindResult<?> bindResult) {
		validators.forEach(validator -> validator.validate(bindResult));
	}
}
