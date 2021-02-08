package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;
import com.google.auto.service.AutoService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.Value;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.Set;

@Value
@AutoService(ConfijValidator.class)
public class JSR303Validator<T> implements ConfijValidator<T> {
	Validator validator = newValidator();

	protected Validator newValidator() {
		return Validation.byProvider(HibernateValidator.class)
				.configure()
				.failFast(false)
				.getterPropertySelectionStrategy(new NoPrefixGetterPropertySelectionStrategy())
				.messageInterpolator(new ParameterMessageInterpolator())
				.buildValidatorFactory()
				.getValidator();
	}

	@Override
	public void validate(BindingResult<T> bindingResult) {
		T config = bindingResult.getValue();
		final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(config);
		if (!constraintViolations.isEmpty()) {
			ConstraintViolationException originalException = new ConstraintViolationException(constraintViolations);
			throw new ConfijValidationException("validation failed for {} with {}", config, constraintViolations, originalException);
		}
	}
}
