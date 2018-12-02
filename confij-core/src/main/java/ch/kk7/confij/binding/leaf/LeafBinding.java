package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionLeaf;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.ConfijNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class LeafBinding<T> implements ConfigBinding<T> {
	@NonNull
	private final ValueMapperInstance<T> valueMapper;

	@Override
	public NodeDefinitionLeaf describe(NodeBindingContext nodeBindingContext) {
		return new NodeDefinitionLeaf(nodeBindingContext);
	}

	@Override
	public T bind(ConfijNode leaf) {
		String value = leaf.getConfig()
				.getNodeBindingContext()
				.getVariableResolver()
				.resolveLeaf(leaf);
		return valueMapper.fromString(value);
	}
}
