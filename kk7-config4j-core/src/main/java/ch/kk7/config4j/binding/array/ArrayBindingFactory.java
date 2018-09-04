package ch.kk7.config4j.binding.array;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import com.fasterxml.classmate.ResolvedType;

import java.util.Optional;

public class ArrayBindingFactory implements ConfigBindingFactory<ArrayBinding> {
	@Override
	public Optional<ArrayBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isArray()) {
			return Optional.of(new ArrayBinding(bindingType.bindingFor(type.getArrayElementType()), configBinder));
		}
		return Optional.empty();
	}
}
