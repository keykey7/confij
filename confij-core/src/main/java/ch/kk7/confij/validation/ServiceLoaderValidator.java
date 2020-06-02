package ch.kk7.confij.validation;

import ch.kk7.confij.tree.ConfijNode;

import java.util.List;

import static ch.kk7.confij.common.ServiceLoaderUtil.instancesOf;

// NOT detectable by serviceLoader himself
public class ServiceLoaderValidator implements ConfijValidator {
	private final List<ConfijValidator> validators;

	public ServiceLoaderValidator() {
		validators = instancesOf(ConfijValidator.class);
	}

	@Override
	public void validate(Object config, ConfijNode confijNode) {
		validators.forEach(validator -> validator.validate(config, confijNode));
	}

	@Override
	public void validate(Object config) {
		validators.forEach(validator -> validator.validate(config));
	}
}
