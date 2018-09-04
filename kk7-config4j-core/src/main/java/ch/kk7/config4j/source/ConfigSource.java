package ch.kk7.config4j.source;

import ch.kk7.config4j.source.simple.SimpleConfig;

public interface ConfigSource {
	void override(SimpleConfig simpleConfig);
}
