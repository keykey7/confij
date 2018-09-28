package ch.kk7.config4j.validation;

import ch.kk7.config4j.binding.intf.InterfaceInvocationHandler;
import ch.kk7.config4j.binding.intf.InterfaceInvocationHandler.Config4jHandled;
import com.google.auto.service.AutoService;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoService(IValidator.class)
public class JSR303Validator implements IValidator {
	private final Validator validator;

	public JSR303Validator() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(new ParameterMessageInterpolator())
				.buildValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@Override
	public void validate(Object config) {
		final Set<ConstraintViolation<Object>> constraintViolations;
		if (config instanceof InterfaceInvocationHandler.Config4jHandled) {
			ExecutableValidator executableValidator = validator.forExecutables();
			constraintViolations = new LinkedHashSet<>();
			((Config4jHandled) config).methodToValue()
					.forEach((k, v) -> constraintViolations.addAll(executableValidator.validateReturnValue(config, k, v)));
		} else {
			constraintViolations = validator.validate(config);
		}
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	protected void validateProxy(InterfaceInvocationHandler.Config4jHandled config) {

	}
}
