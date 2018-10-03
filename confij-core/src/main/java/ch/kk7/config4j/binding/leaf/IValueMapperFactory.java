package ch.kk7.config4j.binding.leaf;

import ch.kk7.config4j.binding.BindingType;

import java.util.Optional;

@FunctionalInterface
public interface IValueMapperFactory {
	Optional<IValueMapper<?>> maybeForType(BindingType bindingType);
}
