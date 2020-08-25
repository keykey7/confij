package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.ConfijBindingException;
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
	public BindingResult<T> bind(ConfijNode leafNode) {
		String strValue = leafNode.getConfig()
				.getNodeBindingContext()
				.getValueResolver()
				.resolveLeaf(leafNode);
		final T converted;
		try {
			converted = valueMapper.fromString(strValue);
		} catch (Exception e) {
			throw new ConfijBindingException(
					"failed to convert string '{}' to an actual configuration object at '{}'. message: {}",
					strValue, leafNode.getUri(), e.getMessage(), e);
		}
		return BindingResult.ofLeaf(converted, leafNode);
	}
}
