package ch.kk7.confij.source.file.format;

import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatList;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.source.Config4jSourceException;
import ch.kk7.confij.source.simple.ConfijNode;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AutoService(ResourceFormat.class)
public class PropertiesFormat implements ResourceFormat {
	private String separator = ".";
	private String globalPrefix = "";
	private static final String VALUE_ITSELF = "";

	public void setSeparator(String separator) {
		this.separator = Objects.requireNonNull(separator);
	}

	public void setPrefix(String globalPrefix) {
		this.globalPrefix = Objects.requireNonNull(globalPrefix);
	}

	@Override
	public void override(ConfijNode confijNode, String configAsStr) {
		final Properties properties = new Properties();
		try (StringReader r = new StringReader(configAsStr)) {
			properties.load(r);
		} catch (IOException e) {
			throw FormatParsingException.invalidFormat("properties", "cannot parse from string: {}", configAsStr);
		}
		overrideWithProperties(confijNode, properties);
	}

	@SuppressWarnings("unchecked")
	protected void overrideWithProperties(ConfijNode simpleConfig, Properties properties) {
		overrideWithFlatMap(simpleConfig, (Map) properties);
	}

	protected void overrideWithFlatMap(ConfijNode simpleConfig, Map<String, String> map) {
		Object deepMap = flatToPrefixedNestedMap(simpleConfig.getConfig(), map);
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	protected void overrideWithDeepMap(ConfijNode simpleConfig, Object deepMap) {
		ConfijNode newConfig = ConfijNode.newRootFor(simpleConfig.getConfig())
				.initializeFromMap(deepMap);
		simpleConfig.overrideWith(newConfig);
	}

	protected Object flatToPrefixedNestedMap(ConfigFormat format, Map<String, String> globalMap) {
		return flatToNestedMap(format, submapWithoutPrefix(globalMap, globalPrefix));
	}

	// TODO: requires some refactoring to get rid of instanceof-nightmare
	protected Object flatToNestedMap(ConfigFormat format, Map<String, String> map) {
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
				.map(key -> key.split(Pattern.quote(separator), 2)[0])
				.map(numpart -> {
					try {
						return Integer.parseInt(numpart);
					} catch (NumberFormatException e) {
						throw new Config4jSourceException("invalid config key, expected a number, but found {}", numpart, e);
					}
				})
				.distinct()
				.sorted()
				.forEach(i -> result.add(i, flatToNestedMap(format.formatForChild("" + i), submapWithoutPrefix(map, "" + i))));
		return result;
	}

	protected Map<String, Object> flatToMap(ConfigFormatMap format, Map<String, String> map) {
		return map.keySet()
				.stream()
				.map(key -> key.split(Pattern.quote(separator), 2)[0])
				.distinct()
				.collect(Collectors.toMap(k -> k, k -> flatToNestedMap(format.formatForChild(k), submapWithoutPrefix(map, k))));
	}

	protected Map<String, String> submapWithoutPrefix(Map<String, String> map, String prefix) {
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
		return result;
	}

	@Override
	public boolean canHandle(URI path) {
		return path.getSchemeSpecificPart()
				.matches("(?s).+\\.prop(ertie)?s?$");
	}
}
