package ch.kk7.config4j.source.simple;

import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static ch.kk7.config4j.common.Util.assertClass;
import static ch.kk7.config4j.source.simple.SimpleConfigException.classOf;

public class SimpleConfigMap extends SimpleConfig {
	private final Map<String, SimpleConfig> map = new LinkedHashMap<>();

	public SimpleConfigMap(ConfigFormat config) {
		super(config);
	}

	protected SimpleConfigMap(SimpleConfig parent, String key) {
		super(parent, key);
	}

	@Override
	public ch.kk7.config4j.source.simple.SimpleConfigMap asMap() {
		return this;
	}

	@Override
	public SimpleConfig resolve(String... path) {
		if (Objects.requireNonNull(path).length == 0) {
			return this;
		}
		String key = path[0];
		SimpleConfig value = map.get(key);
		if (value == null) {
			throw SimpleConfigException.newResolvePathException(getUri(), key, map.keySet());
		}
		String[] subPath = Arrays.copyOfRange(path, 1, path.length);
		return value.resolve(subPath);
	}

	@Override
	public ConfigFormatMap getConfig() {
		return (ConfigFormatMap) super.getConfig();
	}

	public Map<String, SimpleConfig> map() {
		return Collections.unmodifiableMap(map);
	}

	public SimpleConfigList putList(String key) {
		SimpleConfigList node = new SimpleConfigList(this, key);
		map.put(key, node);
		return node;
	}

	public ch.kk7.config4j.source.simple.SimpleConfigMap putMap(String key) {
		ch.kk7.config4j.source.simple.SimpleConfigMap node = new ch.kk7.config4j.source.simple.SimpleConfigMap(this, key);
		map.put(key, node);
		return node;
	}

	public SimpleConfigLeaf putLeaf(String key) {
		SimpleConfigLeaf node = new SimpleConfigLeaf(this, key);
		map.put(key, node);
		return node;
	}

	public void fromObject(Map<?, ?> config) {
		config.forEach((kObj, v) -> {
			if (!(kObj instanceof String)) {
				throw new SimpleConfigException(
						"cannot convert config object into simple format: map contains a key {}, which is of class {}, but only String is allowed",
						kObj, classOf(kObj));
			}
			String k = String.valueOf(kObj);
			if (v instanceof String || v == null) {
				putLeaf(k).set((String) v);
			} else if (v instanceof List) {
				putList(k).fromObject((List<?>) v);
			} else if (v instanceof Map) {
				putMap(k).fromObject((Map<?, ?>) v);
			} else {
				throw new SimpleConfigException(
						"cannot convert config object into simple format: map contains element {}={}, and value is of unhandled class {}",
						k, v, classOf(v));
			}
		});
	}

	@Override
	public Map<String, Object> toObject() {
		Map<String, Object> obj = new LinkedHashMap<>(map.size());
		map.forEach((k, v) -> obj.put(k, v.toObject()));
		return Collections.unmodifiableMap(obj);
	}

	@Override
	public void overrideWith(SimpleConfig simpleConfig) {
		ch.kk7.config4j.source.simple.SimpleConfigMap other = assertClass(simpleConfig,
				ch.kk7.config4j.source.simple.SimpleConfigMap.class);
		other.map.forEach((k, v) -> {
			if (map.containsKey(k)) {
				map.get(k)
						.overrideWith(v);
			} else {
				map.put(k, v);
			}
		});
	}

	@Override
	protected Object hashMe() {
		return map;
	}

	@Override
	public Stream<SimpleConfigLeaf> leaves() {
		return map.values()
				.stream()
				.flatMap(SimpleConfig::leaves);
	}
}
