package ch.kk7.confij.validation;

import ch.kk7.confij.tree.ConfijNode;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Arrays;
import java.util.List;

@Value
@NonFinal
public class MultiValidator implements ConfijValidator {
	List<ConfijValidator> validators;

	public static MultiValidator of(ConfijValidator... validators) {
		return new MultiValidator(Arrays.asList(validators));
	}

	@Override
	public void validate(Object config, ConfijNode confijNode) {
		validators.forEach(x -> x.validate(config, confijNode));
	}

	@Override
	public void validate(Object config) throws ConfijValidationException {
		validators.forEach(x -> x.validate(config));
	}
}
