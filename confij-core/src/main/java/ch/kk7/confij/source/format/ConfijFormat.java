package ch.kk7.confij.source.format;

import ch.kk7.confij.tree.ConfijNode;

public interface ConfijFormat {
	void override(ConfijNode rootNode, String configAsStr);
}
