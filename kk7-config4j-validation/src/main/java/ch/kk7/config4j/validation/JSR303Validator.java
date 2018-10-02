package ch.kk7.config4j.validation;

import com.google.auto.service.AutoService;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@AutoService(IValidator.class)
public class JSR303Validator implements IValidator {
	private final Validator validator;

	public static class NoPrefixGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {
		@Override
		public Optional<String> getProperty(ConstrainableExecutable executable) {
			if (executable.getReturnType() == void.class || executable.getParameterTypes().length > 0) {
				return Optional.empty();
			}
			return Optional.of(executable.getName());
		}

		@Override
		public Set<String> getGetterMethodNameCandidates(String propertyName) {
			return Collections.singleton(propertyName);
		}
	}

	public JSR303Validator() {
		validator = Validation.byProvider(HibernateValidator.class)
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
