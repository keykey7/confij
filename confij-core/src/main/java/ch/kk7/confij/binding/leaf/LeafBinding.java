package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class LeafBinding<T> implements ConfigBinding<T> {
	@NonNull
	private final ValueMapperInstance<T> valueMapper;

	@Override
	public ConfigFormatLeaf describe(FormatSettings formatSettings) {
		return new ConfigFormatLeaf(formatSettings);
	}

	@Override
	public T bind(ConfijNode leaf) {
		String value = leaf.getConfig()
				.getFormatSettings()
				.getVariableResolver()
				.resolveLeaf(leaf);
		return valueMapper.fromString(value);
	}
}
