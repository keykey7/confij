package ch.kk7.config4j.binding.leaf;

import ch.kk7.config4j.binding.BindingType;

import java.util.Optional;

public interface ValueMapperFactory {
	Optional<IValueMapper<?>> maybeForType(BindingType type);
}
