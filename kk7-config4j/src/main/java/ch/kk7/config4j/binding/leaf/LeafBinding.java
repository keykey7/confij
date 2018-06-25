package ch.kk7.config4j.binding.leaf;

import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.binding.leaf.mapper.ValueMapper;
import ch.kk7.config4j.binding.leaf.mapper.ValueMapperFactory;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigLeaf;
import com.fasterxml.classmate.ResolvedType;

import java.util.Optional;

public class LeafBinding<T> implements ConfigBinding<T> {
	private final ValueMapper<T> valueMapper;

	public LeafBinding(ValueMapper<T> valueMapper) {
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

	public static class LeafBindingFactory implements ConfigBindingFactory<LeafBinding> {
		private final ValueMapperFactory factory;

		public LeafBindingFactory(ValueMapperFactory factory) {
			this.factory = factory;
		}

		@Override
		public Optional<LeafBinding> maybeCreate(ResolvedType type, ConfigBinder configBinder) {
			return factory.maybeForType(type)
					.map(LeafBinding::new);
		}
	}
}
