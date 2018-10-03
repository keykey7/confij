package ch.kk7.confij.binding.array;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
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
