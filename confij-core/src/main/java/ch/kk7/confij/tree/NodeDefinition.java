package ch.kk7.confij.tree;

import ch.kk7.confij.binding.ConfijBindingException;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The definition of how the config must look like on a high level, like
 * what children it can and must contain. No link to actual config values.
 */
@Value
@NonFinal
public abstract class NodeDefinition {
	@NonNull
	private final NodeBindingContext nodeBindingContext;

	/**
	 * @return true if this is a leaf node and can hold a property value
	 */
	public boolean isValueHolder() {
		return false;
	}

	/**
	 * @param configKey the name/key of the child for which we want the definition
	 * @return an instance of self for a given named child node.
	 */
	@NonNull
	public abstract NodeDefinition definitionForChild(String configKey);

	/**
	 * @return a set of required names/keys for child nodes
	 */
	@NonNull
	public Set<String> getMandatoryKeys() {
		return Collections.emptySet();
	}

	@ToString
	public static class NodeDefinitionLeaf extends NodeDefinition {
		public NodeDefinitionLeaf(NodeBindingContext nodeBindingContext) {
			super(nodeBindingContext);
		}

		@NonNull
		@Override
		public NodeDefinition definitionForChild(String configKey) {
			throw new ConfijBindingException("a leaf node isn't allowed to have children, not even for '{}'", configKey);
		}

		@Override
		public boolean isValueHolder() {
			return true;
		}
	}

	@ToString
	public static class NodeDefinitionList extends NodeDefinition {
		private final NodeDefinition anyChild;

		public NodeDefinitionList(NodeBindingContext nodeBindingContext, NodeDefinition anyChild) {
			super(nodeBindingContext);
			this.anyChild = Objects.requireNonNull(anyChild);
		}

		@NonNull
		@Override
		public NodeDefinition definitionForChild(String configKey) {
			final int index;
			try {
				index = Integer.parseInt(configKey);
			} catch (NumberFormatException e) {
				throw new ConfijBindingException("invalid config key, expected an integer, but found {}", configKey, e);
			}
			if (index < 0) {
				throw new ConfijBindingException("invalid config key, expected a positive number, but found {}", index);
			}
			return anyChild;
		}
	}

	@ToString
	public static class NodeDefinitionMap extends NodeDefinition {
		private final NodeDefinition anyChild;
		private final Map<String, NodeDefinition> children;

		protected NodeDefinitionMap(NodeBindingContext nodeBindingContext, NodeDefinition anyChild, Map<String, NodeDefinition> children) {
			super(nodeBindingContext);
			this.anyChild = anyChild;
			if (children.containsValue(null)) {
				throw new IllegalArgumentException("invalid null value in " + children);
			}
			this.children = Collections.unmodifiableMap(children);
		}

		/**
		 * a map-like definition for a node where all keys are known and no optional ones are allowed.
		 * @param nodeBindingContext this nodes context
		 * @param children the map of mandatory child definitions
		 * @return self
		 */
		public static NodeDefinitionMap fixedKeysMap(NodeBindingContext nodeBindingContext, Map<String, NodeDefinition> children) {
			return new NodeDefinitionMap(nodeBindingContext, null, children);
		}

		public static NodeDefinitionMap anyKeyMap(NodeBindingContext nodeBindingContext, NodeDefinition anyChild) {
			return new NodeDefinitionMap(nodeBindingContext, Objects.requireNonNull(anyChild), Collections.emptyMap());
		}

		@NonNull
		@Override
		public NodeDefinition definitionForChild(String configKey) {
			if (children.containsKey(configKey)) {
				return children.get(configKey);
			}
			if (anyChild != null) {
				return anyChild;
			}
			throw new ConfijBindingException("invalid config key: map-like format doesn't allow key '{}', allowed are: {}", configKey,
					children.keySet());
		}

		@NonNull
		@Override
		public Set<String> getMandatoryKeys() {
			return children.keySet();
		}
	}
}
