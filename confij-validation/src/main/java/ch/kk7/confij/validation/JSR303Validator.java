package ch.kk7.confij.validation;

import com.google.auto.service.AutoService;
import lombok.Value;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Value
@AutoService(ConfijValidator.class)
public class JSR303Validator implements ConfijValidator {
	Validator validator = newValidator();

	protected Validator newValidator() {
		return Validation.byProvider(HibernateValidator.class)
				.configure()
				.getterPropertySelectionStrategy(new NoPrefixGetterPropertySelectionStrategy())
				.messageInterpolator(new ParameterMessageInterpolator())
				.buildValidatorFactory()
				.getValidator();
	}

	@Override
	public void validate(Object config) {
		final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(config);
		if (!constraintViolations.isEmpty()) {
			ConstraintViolationException originalException = new ConstraintViolationException(constraintViolations);
			throw new ConfijValidationException("validation failed for {} with {}", config, constraintViolations, originalException);
		}
	}
}
