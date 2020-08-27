package ch.kk7.confij.source.format;

import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeDefinition;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Map.Entry;

@UtilityClass
public class MapAndStringValidator {
	private final String SEP = ".";

	public void validateDefinition(Object src, ConfijNode node) {
		validateObj(null, src, node.getConfig());
	}

	@SuppressWarnings("unchecked")
	protected void validateObj(String path, Object src, NodeDefinition definition) {
		if (src instanceof String || src == null) {
			validateDefinition(path, (String) src, definition);
		} else if (src instanceof Map) {
			validateDefinition(path, (Map) src, definition);
		} else {
			throw new IllegalArgumentException("expected an instance of String or Map at config path " + path + ", but got " + src);
		}
	}

	protected void validateDefinition(String path, Map<String, Object> src, NodeDefinition definition) {
		// note: the src is not required to have all mandatory keys
		for (Entry<String, Object> entry : src.entrySet()) {
			String childPath = (path == null ? "" : path + SEP) + entry.getKey();
			final NodeDefinition childDefinition;
			try {
				childDefinition = definition.definitionForChild(entry.getKey());
			} catch (ConfijBindingException e) {
				throw new ConfijBindingException("unexpected content at configuration path '{}' (value: {}): " + e.getMessage(), childPath,
						src, e);
			}
			validateObj(childPath, entry.getValue(), childDefinition);
		}
	}

	protected void validateDefinition(String path, String src, NodeDefinition definition) {
		if (!definition.isValueHolder()) {
			throw new ConfijBindingException("unexpected leaf-value at key '{}' (value: {}). expected a Map instead. mandatory keys are {}",
					path == null ? SEP : path, src, definition.getMandatoryKeys());
		}
	}
}
