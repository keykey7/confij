package ch.kk7.confij.source.file.format;

import ch.kk7.confij.source.simple.ConfijNode;

import java.net.URI;

public interface ResourceFormat {
	void override(ConfijNode confijNode, String configAsStr);

	boolean canHandle(URI path);
}
