package ch.kk7.config4j.format.validation;

import ch.kk7.config4j.source.simple.SimpleConfig;

public interface IValidator {
	void validate(SimpleConfig simpleConfig);
}
