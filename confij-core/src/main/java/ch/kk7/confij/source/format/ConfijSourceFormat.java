package ch.kk7.confij.source.format;

import ch.kk7.confij.tree.ConfijNode;

import java.net.URI;

public interface ConfijSourceFormat {
	void override(ConfijNode confijNode, String configAsStr);

	boolean canHandle(URI path);
}
