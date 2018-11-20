package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;

import java.util.Optional;

public class ForcedLeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
	@Override
	public Optional<LeafBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		return bindingType.getBindingContext()
				.getForcedMapperFactory()
				.map(iValueMapperFactory -> iValueMapperFactory.maybeForType(bindingType)
						.orElseThrow(() -> new BindingException(
								"forced a ValueMapping, but factory {} didn't return a Mapping for bindingType {}", iValueMapperFactory,
								bindingType)))
				.map(LeafBinding::new);
	}
}
