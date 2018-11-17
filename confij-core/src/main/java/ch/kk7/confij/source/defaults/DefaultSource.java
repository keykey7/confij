package ch.kk7.confij.source.defaults;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.tree.ConfijNode;

import java.util.Set;

/**
 * This is a special ConfigSource since it does not simply override existing values.
 * It rather extends them, by introducing missing nodes and setting default values.
 */
public class DefaultSource implements ConfigSource {
	@Override
	public void override(ConfijNode confijNode) {
		if (confijNode.getConfig()
				.isValueHolder()) {
			overrideLeaf(confijNode);
		} else {
			overrideBranch(confijNode);
		}
	}

	protected void overrideLeaf(ConfijNode leafNode) {
		String currentValue = leafNode.getValue();
		if (currentValue == null) {
			leafNode.setValue(leafNode.getConfig()
					.getFormatSettings()
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
