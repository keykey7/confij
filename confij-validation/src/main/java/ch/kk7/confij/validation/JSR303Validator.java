package ch.kk7.confij.validation;

import com.google.auto.service.AutoService;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@AutoService(IValidator.class)
public class JSR303Validator implements IValidator {
	private final Validator validator;

	public JSR303Validator() {
		validator = newValidator();
	}

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
		// TODO: patch rootBeanClass from rootBeanClass=class com.sun.proxy.$Proxy16 to something readable
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}
}
