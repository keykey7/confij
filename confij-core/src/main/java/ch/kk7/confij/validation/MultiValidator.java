package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Arrays;
import java.util.List;

@Value
@NonFinal
public class MultiValidator<T> implements ConfijValidator<T> {
	List<ConfijValidator<T>> validators;

	@SafeVarargs
	public static <T> MultiValidator<T> of(ConfijValidator<T>... validators) {
		return new MultiValidator<>(Arrays.asList(validators));
	}

	@Override
	public void validate(BindingResult<T> bindingResult) {
		validators.forEach(x -> x.validate(bindingResult));
	}
}
