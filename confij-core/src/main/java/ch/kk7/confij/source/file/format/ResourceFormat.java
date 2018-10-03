package ch.kk7.confij.source.file.format;

import ch.kk7.confij.source.simple.SimpleConfig;

import java.net.URI;

public interface ResourceFormat {
	void override(SimpleConfig simpleConfig, String configAsStr);

	boolean canHandle(URI path);
}
