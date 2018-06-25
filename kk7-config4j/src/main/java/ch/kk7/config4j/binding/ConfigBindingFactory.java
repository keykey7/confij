package ch.kk7.config4j.binding;

import com.fasterxml.classmate.ResolvedType;

import java.util.Optional;

public interface ConfigBindingFactory<T extends ConfigBinding> {
	Optional<T> maybeCreate(ResolvedType type, ConfigBinder configBinder);
}
