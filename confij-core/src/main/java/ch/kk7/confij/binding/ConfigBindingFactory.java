package ch.kk7.confij.binding;

import java.util.Optional;

public interface ConfigBindingFactory<T extends ConfigBinding> {
	Optional<T> maybeCreate(BindingType bindingType, ConfigBinder configBinder);
}
