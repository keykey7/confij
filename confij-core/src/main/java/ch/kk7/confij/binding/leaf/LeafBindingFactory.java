package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;

import java.util.Optional;

public class LeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
	public static Optional<ValueMapperInstance> firstValueMapper(BindingType bindingType) {
		return bindingType.getBindingContext()
				.getMapperFactories()
				.stream()
				.map(iValueMapperFactory -> iValueMapperFactory.maybeForType(bindingType))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ValueMapperInstance.class::cast)
				.findFirst();
	}

	@Override
	public Optional<LeafBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		return firstValueMapper(bindingType).map(LeafBinding::new);
	}
}
