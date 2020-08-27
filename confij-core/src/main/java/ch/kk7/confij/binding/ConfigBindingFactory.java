package ch.kk7.confij.binding;

import java.util.Optional;

public interface ConfigBindingFactory<T extends ConfigBinding> {
	/**
	 * @param bindingType  java-type and binding context to be processed
	 * @param configBinder the binder holding together all the factories
	 * @return a binding if this factory can process this bindingType, empty otherwise
	 * @throws ConfijDefinitionException when the interface itself is invalid
	 */
	Optional<T> maybeCreate(BindingType bindingType, ConfigBinder configBinder);
}
