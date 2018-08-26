package ch.kk7.config4j.binding;

import java.util.Optional;

public interface ConfigBindingFactory<T extends ConfigBinding> {
	Optional<T> maybeCreate(BindingType type, ConfigBinder configBinder);
}
