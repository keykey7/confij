package ch.kk7.confij.source.defaults;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.tree.ConfijNode;

import java.util.Set;

/**
 * This is a special ConfigSource since it does not simply override existing values.
 * It rather extends them, by introducing missing nodes and setting default values
 * (all taken from code and not from external).
 */
public class DefaultSource implements ConfijSource {
	@Override
	public void override(ConfijNode rootNode) {
		if (rootNode.getConfig()
				.isValueHolder()) {
			overrideLeaf(rootNode);
		} else {
			overrideBranch(rootNode);
		}
	}

	protected void overrideLeaf(ConfijNode leafNode) {
		String currentValue = leafNode.getValue();
		if (currentValue == null) {
			leafNode.setValue(leafNode.getConfig()
					.getNodeBindingContext()
					.getDefaultValue()); // might still be null!
		}
	}

	protected void overrideBranch(ConfijNode confijBranch) {
		Set<String> existingKeys = confijBranch.getChildren()
				.keySet();
		confijBranch.getChildren()
				.values()
				.forEach(this::override);

		// additionally to the existing values we have to add mandatory keys which are not yet present
		confijBranch.getConfig()
				.getMandatoryKeys()
				.stream()
				.filter(Util.not(existingKeys::contains))
				.map(confijBranch::addChild)
				.forEach(this::override);
	}
}
