package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;

import java.util.Optional;

public class LeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
	@Override
	public Optional<LeafBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		return bindingType.getBindingContext()
				.getMapperFactories()
				.stream()
				.map(iValueMapperFactory -> iValueMapperFactory.maybeForType(bindingType))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.map(LeafBinding::new);
	}
}
