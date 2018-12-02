package ch.kk7.confij.source;

import ch.kk7.confij.tree.ConfijNode;

public interface ConfigSource {
	void override(ConfijNode rootNode);
}
