package ch.kk7.confij.source.file.format;

import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.source.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AutoService(ConfijSourceFormat.class)
public class PropertiesFormat implements ConfijSourceFormat {
	@NonNull
	@Setter
	private String separator = ".";
	@NonNull
	@Setter
	private String globalPrefix = "";

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
		Object deepMap = flatToNestedMapWithPrefix(simpleConfig.getConfig(), map);
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	protected void overrideWithDeepMap(ConfijNode node, Object deepMap) {
		ConfijNode newConfig = ConfijNode.newRootFor(node.getConfig())
				.initializeFromMap(deepMap);
		node.overrideWith(newConfig);
	}

	protected Object flatToNestedMapWithPrefix(ConfigFormat format, Map<String, String> globalMap) {
		if (format.isValueHolder()) {
			return globalMap.get(globalPrefix);
		}
		return flatToNestedMap(format, flatmapPrefixedBy(globalMap, globalPrefix));
	}

	/**
	 * for each {@code k}:<br>
	 * - if it is a leaf node: return the value as String (nullable)<br>
	 * - otherwise: return the branch as Map<String, Object> (nonnull)<br>
	 */
	protected Function<String, Object> valueMapper(ConfigFormat parentFormat, Map<String, String> map) {
		return k -> {
			ConfigFormat childFormat = parentFormat.formatForChild(k);
			if (childFormat.isValueHolder()) {
				return map.get(k);
			}
			Map<String, String> childMap = flatmapPrefixedBy(map, k);
			return flatToNestedMap(childFormat, childMap);
		};
	}

	@NonNull
	protected Object flatToNestedMap(ConfigFormat format, Map<String, String> map) {
		return map.keySet()
				.stream()
				.map(key -> key.split(Pattern.quote(separator), 2)[0]) // extract all prefixes
				.distinct() // handle each prefix only once
				.collect(Collectors.toMap(k -> k, valueMapper(format, map)));
	}

	@NonNull
	protected Map<String, String> flatmapPrefixedBy(@NonNull Map<String, String> map, @NonNull String prefix) {
		if (prefix.isEmpty()) {
			return map;
		}
		String prefixAndSep = prefix + separator;
		if (map.containsKey(prefix)) {
			throw new IllegalArgumentException(
					"invalid key '" + prefix + "' in map " + map + ". Expected are only keys like '" + prefixAndSep + "*'");
		}
		return map.entrySet()
				.stream()
				.filter(e -> e.getKey()
						.startsWith(prefixAndSep))
				.collect(Collectors.toMap(e -> e.getKey()
						.substring(prefixAndSep.length()), Entry::getValue, (x, y) -> x, HashMap::new));
	}

	@Override
	public boolean canHandle(URI path) {
		return path.getSchemeSpecificPart()
				.matches("(?s).+\\.prop(ertie)?s?$");
	}
}
