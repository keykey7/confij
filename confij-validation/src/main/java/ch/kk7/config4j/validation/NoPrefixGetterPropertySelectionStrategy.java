package ch.kk7.config4j.validation;

import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class NoPrefixGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {
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
