package ch.kk7.config4j.binding.leaf;

import ch.kk7.config4j.binding.BindingException;
import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigLeaf;

import java.util.Optional;

public class LeafBinding<T> implements ConfigBinding<T> {
	private final IValueMapper<T> valueMapper;

	public LeafBinding(IValueMapper<T> valueMapper) {
		this.valueMapper = valueMapper;
	}

	@Override
	public ConfigFormatLeaf describe(FormatSettings formatSettings) {
		return new ConfigFormatLeaf(formatSettings);
	}

	@Override
	public T bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigLeaf)) {
			throw new IllegalStateException("expected a leaf, but got: " + config);
		}
		return valueMapper.fromString(((SimpleConfigLeaf) config).get());
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
