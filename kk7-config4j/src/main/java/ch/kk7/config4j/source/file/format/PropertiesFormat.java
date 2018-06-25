package ch.kk7.config4j.source.file.format;

import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.config4j.source.Config4jSourceException;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesFormat implements ResourceFormat {
	private String separator = ".";
	private String globalPrefix = "config";
	private static final String VALUE_ITSELF = "";

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setPrefix(String globalPrefix) {
		this.globalPrefix = globalPrefix;
	}

	@Override
	public void override(SimpleConfig simpleConfig, String configAsStr) {
		final Properties properties = new Properties();
		try (StringReader r = new StringReader(configAsStr)) {
			properties.load(r);
		} catch (IOException e) {
			throw FormatParsingException.invalidFormat("properties", "cannot parse from string: {}", configAsStr);
		}
		overrideWithProperties(simpleConfig, properties);
	}

	@SuppressWarnings("unchecked")
	protected void overrideWithProperties(SimpleConfig simpleConfig, Properties properties) {
		overrideWithFlatMap(simpleConfig, (Map) properties);
	}

	protected void overrideWithFlatMap(SimpleConfig simpleConfig, Map<String, String> map) {
		Object deepMap = flatToDeepMap(simpleConfig.getConfig(), map);
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	protected void overrideWithDeepMap(SimpleConfig simpleConfig, Object deepMap) {
		SimpleConfig newConfig = SimpleConfig.fromObject(deepMap, simpleConfig.getConfig());
		simpleConfig.overrideWith(newConfig);
	}

	protected Object flatToDeepMap(ConfigFormat format, Map<String, String> globalMap) {
		return flatToWhatever(format, submapOf(globalMap, globalPrefix));
	}

	protected Object flatToWhatever(ConfigFormat format, Map<String, String> map) {
		if (format instanceof ConfigFormatLeaf) {
			return map.get(VALUE_ITSELF);
		} else if (format instanceof ConfigFormatList) {
			return flatToList((ConfigFormatList) format, map);
		} else if (format instanceof ConfigFormatMap) {
			return flatToMap((ConfigFormatMap) format, map);
		} else {
			throw new IllegalStateException("cannot handle format " + format);
		}
	}

	protected List<Object> flatToList(ConfigFormatList format, Map<String, String> map) {
		List<Object> result = new LinkedList<>();
		map.keySet()
				.stream()
				.map(key -> key.split(separator, 2)[0])
				.map(numpart -> {
					try {
						return Integer.parseInt(numpart);
					} catch (NumberFormatException e) {
						throw new Config4jSourceException("invalid config key, expected a number, but found {}", numpart, e);
					}
				})
				.distinct()
				.forEach(i -> result.add(i, flatToWhatever(format.anyChild(), submapOf(map, "" + i))));
		return result;
	}

	protected Map<String, Object> flatToMap(ConfigFormatMap format, Map<String, String> map) {
		return map.keySet()
				.stream()
				.map(key -> key.split(separator, 2)[0])
				.distinct()
				.collect(Collectors.toMap(k -> k, k -> flatToWhatever(format.get(k)
						.orElseThrow(() -> new Config4jSourceException("invalid config key {} is not allowed", k)), submapOf(map, k))));
	}

	protected Map<String, String> submapOf(Map<String, String> map, String prefix) {
		if (prefix.isEmpty()) {
			return map;
		}
		String prefixAndSep = prefix + separator;
		Map<String, String> result = map.entrySet()
				.stream()
				.filter(e -> e.getKey()
						.startsWith(prefixAndSep))
				.collect(Collectors.toMap(e -> e.getKey()
						.substring(prefixAndSep.length()), Entry::getValue, (x, y) -> x, HashMap::new));
		// Note: a (stupid) key "<prefix>." is overridden here by key "<prefix>"
		Optional.ofNullable(map.get(prefix))
				.ifPresent(v -> result.put(VALUE_ITSELF, v));
		return map;
	}
}
