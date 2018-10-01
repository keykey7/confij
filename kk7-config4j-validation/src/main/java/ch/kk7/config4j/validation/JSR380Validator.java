package ch.kk7.config4j.validation;

import ch.kk7.config4j.binding.ConfigBinding.BindResult;
import ch.kk7.config4j.binding.intf.InterfaceInvocationHandler;
import ch.kk7.config4j.binding.intf.InterfaceInvocationHandler.Config4jHandled;
import com.google.auto.service.AutoService;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(IValidator.class)
public class JSR380Validator implements IValidator {
	private final Validator validator;

	// TODO: add custom metaDataProvider to always set @Valid and @NotNull

	public JSR380Validator() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(new ParameterMessageInterpolator())
				.buildValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@Override
	public void validate(BindResult bindResult) {
		final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bindResult.getValue());
		//final Set<ConstraintViolation<Object>> constraintViolations = validateRecursive("root", bindResult);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	protected <X> Set<ConstraintViolation<?>> validateRecursive(String path, BindResult<X> bindResult) {
		X value = bindResult.getValue();
		Set<ConstraintViolation<X>> objViolations = validateObject(value).stream()
				.map(cv -> mapConstraintViolation(cv, path, cv.getRootBeanClass()))
				.collect(Collectors.toSet());
		Set<ConstraintViolation<?>> violations = new LinkedHashSet<>(objViolations);
		bindResult.getSiblings().forEach((k,sibling) -> violations.addAll(validateRecursive(path + "." + k, sibling)));
		return violations;
	}

	// TODO: fix the constraintviolation class and path here
	protected <X> Set<ConstraintViolation<X>> validateObject(X config) {
		if (config == null) {
			// TODO: make configurable for all properties (i.e. default to @NotNull)
			return Collections.emptySet();
		}
		if (config instanceof InterfaceInvocationHandler.Config4jHandled) {
			ExecutableValidator executableValidator = validator.forExecutables();
			return ((Config4jHandled) config).methodToValue()
					.entrySet()
					.stream()
					.flatMap(e -> executableValidator.validateReturnValue(config, e.getKey(), e.getValue())
							.stream())
					.collect(Collectors.toSet());
		}
		return validator.validate(config);
	}

	protected <T> ConstraintViolation<T> mapConstraintViolation(ConstraintViolation<T> original, String prefixPath, Class<T> rootBeanClass) {
		if (!(original instanceof ConstraintViolationImpl)) {
			// cannot handle
			return original;
		}
		ConstraintViolationImpl<T> impl = (ConstraintViolationImpl<T>) original;

		PathImpl path = PathImpl.createPathFromString(prefixPath + "." + impl.getPropertyPath().toString().replaceAll("\\.<return value>", ""));
		return ConstraintViolationImpl.forReturnValueValidation(impl.getMessageTemplate(), impl.getMessageParameters(),
				impl.getExpressionVariables(), impl.getMessage(), rootBeanClass, impl.getRootBean(), impl.getLeafBean(), impl.getInvalidValue(),
				path, impl.getConstraintDescriptor(), null, impl.getExecutableReturnValue(), null);
	}
}
