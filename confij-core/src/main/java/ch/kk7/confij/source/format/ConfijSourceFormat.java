package ch.kk7.confij.source.format;

import ch.kk7.confij.source.ConfijSourceBuilder.URIish;
import ch.kk7.confij.tree.ConfijNode;

public interface ConfijSourceFormat {
	void override(ConfijNode rootNode, String configAsStr);

	boolean canHandle(URIish path);
}
