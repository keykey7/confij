package ch.kk7.confij.validation;

import static ch.kk7.confij.common.ServiceLoaderUtil.instancesOf;

import java.util.List;

// NOT detectable by serviceLoader himself
public class ServiceLoaderValidator implements ConfijValidator {
	private final List<ConfijValidator> validators;

	public ServiceLoaderValidator() {
		validators = instancesOf(ConfijValidator.class);
	}

	@Override
	public void validate(Object config) {
		validators.forEach(validator -> validator.validate(config));
	}
}
