package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;

import java.util.Optional;

public class LeafBinding<T> implements ConfigBinding<T> {
	private final ValueMapperInstance<T> valueMapper;

	public LeafBinding(ValueMapperInstance<T> valueMapper) {
		this.valueMapper = valueMapper;
	}

	@Override
	public ConfigFormatLeaf describe(FormatSettings formatSettings) {
		return new ConfigFormatLeaf(formatSettings);
	}

	@Override
	public T bind(ConfijNode config) {
		return valueMapper.fromString(config.getValue());
	}

	public static class ForcedLeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
		@Override
		public Optional<LeafBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
			return bindingType.getBindingSettings()
					.getForcedMapperFactory()
					.map(iValueMapperFactory -> iValueMapperFactory.maybeForType(bindingType)
							.orElseThrow(() -> new BindingException(
									"forced a ValueMapping, but factory {} didn't return a Mapping for bindingType {}", iValueMapperFactory,
									bindingType)))
					.map(LeafBinding::new);
		}
	}

	public static class LeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
		@Override
		public Optional<LeafBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
			return bindingType.getBindingSettings()
					.getMapperFactories()
					.stream()
					.map(iValueMapperFactory -> iValueMapperFactory.maybeForType(bindingType))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst()
					.map(LeafBinding::new);
		}
	}
}
