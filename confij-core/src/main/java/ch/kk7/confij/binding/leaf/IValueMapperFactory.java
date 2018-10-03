package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingType;

import java.util.Optional;

@FunctionalInterface
public interface IValueMapperFactory {
	Optional<IValueMapper<?>> maybeForType(BindingType bindingType);
}
