package ch.kk7.confij.source.format;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.any.ConfijAnyFormat;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Setter
@Getter
public class PropertiesFormat implements ConfijFormat {
	private static final Pattern BRACKETS_ARRAY_FORMAT = Pattern.compile("(\\S+)\\[(\\d+)]");
	@NonNull
	private String separator = ".";
	private String globalPrefix = null;

	@Override
	public void override(ConfijNode rootNode, String configAsStr) {
		final Properties properties = new Properties();
		try (StringReader r = new StringReader(configAsStr)) {
			properties.load(r);
		} catch (IOException e) {
			throw ConfijSourceFormatException.invalidFormat("properties", "cannot load from string", e);
		}
		overrideWithProperties(rootNode, properties);
	}

	@SuppressWarnings("unchecked")
	protected void overrideWithProperties(ConfijNode simpleConfig, Properties properties) {
		overrideWithFlatMap(simpleConfig, (Map) properties);
	}

	protected void overrideWithFlatMap(ConfijNode simpleConfig, Map<String, String> map) {
		Object deepMap = flatToNestedMapWithPrefix(map);
		overrideWithDeepMap(simpleConfig, deepMap);
	}

	protected void overrideWithDeepMap(ConfijNode node, Object deepMap) {
		MapAndStringValidator.validateDefinition(deepMap, node);
		ConfijNode newConfig = ConfijNode.newRootFor(node.getConfig())
				.initializeFromMap(deepMap);
		node.overrideWith(newConfig);
	}

	protected Object flatToNestedMapWithPrefix(Map<String, String> globalMap) {
		return flatToNestedMap(flatmapPrefixedBy(globalMap, getGlobalPrefix()));
	}

	protected Map<String, String> flatmapPrefixedBy(@NonNull Map<String, String> map, String prefix) {
		if (prefix == null) {
			return map;
		}
		String prefixAndSep = prefix + getSeparator();
		Map<String, String> result = new HashMap<>();
		map.forEach((k, v) -> {
			if (k.startsWith(prefixAndSep)) {
				result.put(k.substring(prefixAndSep.length()), v);
			}
		});
		return result;
	}

	@NonNull
	protected Object flatToNestedMap(@NonNull Map<String, String> map) {
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, String> entry : map.entrySet()) {
			String fullKey = entry.getKey();
			Map<String, Object> current = result;
			String[] keyParts = Arrays.stream(fullKey.split(Pattern.quote(getSeparator()), -1))
					.flatMap(currentKey -> {
						Matcher matcher = BRACKETS_ARRAY_FORMAT.matcher(currentKey);
						if (matcher.matches()) {
							return Stream.of(matcher.group(1), matcher.group(2));
						} else {
							return Stream.of(currentKey);
						}
					})
					.toArray(String[]::new);
			String keySoFar = null;
			for (int i = 0; i < keyParts.length - 1; i++) {
				keySoFar = keySoFar == null ? keyParts[i] : keySoFar + getSeparator() + keyParts[i];
				Object child = current.computeIfAbsent(keyParts[i], s -> new HashMap<>());
				if (child instanceof String) {
					throw keyConflict(fullKey, keySoFar);
				}
				current = (Map<String, Object>) child;
			}
			String lastKeyPart = keyParts[keyParts.length - 1];
			if (current.containsKey(lastKeyPart)) {
				throw keyConflict(fullKey, fullKey + getSeparator() + "*");
			}
			current.put(lastKeyPart, entry.getValue());
		}
		return result;
	}

	protected ConfijSourceFormatException keyConflict(String key1, String key2) {
		String prefixStr = getGlobalPrefix() == null ? "" : getGlobalPrefix() + getSeparator();
		return new ConfijSourceFormatException(
				"key '{}' conflicts with key '{}'. each key must start with an unique string to map it into a config-tree structure.",
				prefixStr + key1, prefixStr + key2);
	}

	@ToString
	@AutoService(ConfijAnyFormat.class)
	public static class PropertiesAnyFormat implements ConfijAnyFormat {
		@Override
		public Optional<ConfijFormat> maybeHandle(String pathTemplate) {
			if (Util.getSchemeSpecificPart(pathTemplate)
					.matches("(?s).+\\.prop(ertie)?s?$")) {
				return Optional.of(new PropertiesFormat());
			}
			return Optional.empty();
		}
	}
}
