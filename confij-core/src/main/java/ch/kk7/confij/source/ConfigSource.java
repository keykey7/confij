package ch.kk7.confij.source;

import ch.kk7.confij.source.simple.ConfijNode;

public interface ConfigSource {
	void override(ConfijNode simpleConfig);
}
