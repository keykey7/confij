package ch.kk7.config4j.source.env;

import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.file.format.PropertiesFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.util.Map;
import java.util.Properties;

/**
 * a source where the full set of properties is already known at create time
 */
class FlatSource extends PropertiesFormat implements ConfigSource {
	private final Map<String, String> flatMap;
	private Object deepMap;

	public FlatSource(Map<String, String> map) {
		this.flatMap = map;
	}

	@SuppressWarnings("unchecked")
	public FlatSource(Properties properties) {
		this((Map) properties);
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		if (deepMap == null) {
			deepMap = flatToDeepWithPrefix(simpleConfig.getConfig(), flatMap);
		}
		overrideWithDeepMap(simpleConfig, deepMap);
	}
}
