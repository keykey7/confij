package ch.kk7.confij.source;

import ch.kk7.confij.source.simple.SimpleConfig;

public interface ConfigSource {
	void override(SimpleConfig simpleConfig);
}
