package ch.kk7.confij.source.format;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.any.ConfijAnyFormat;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import static ch.kk7.confij.source.format.ConfijSourceFormatException.invalidFormat;

@Value
public class YamlFormat implements ConfijFormat {
	Yaml yaml = new Yaml(new SafeConstructorWithDateTime());

	@Override
	public void override(ConfijNode rootNode, String content) {
		final Iterable<Object> yamlIterable;
		try {
			yamlIterable = yaml.loadAll(content);
		} catch (Exception e) {
			throw invalidFormat("YAML", "cannot load from string", e);
		}
		yamlIterable.forEach(root -> {
			Object simpleRoot = simplify(root);
			ConfijNode newConfig = ConfijNode.newRootFor(rootNode.getConfig())
					.initializeFromMap(simpleRoot);
			rootNode.overrideWith(newConfig);
		});
	}

	@SuppressWarnings("unchecked")
	protected Object simplify(Object yaml) {
		if (yaml == null) {
			return null;
		}
		if (yaml instanceof Map) {
			return simplifyMap((Map<Object, Object>) yaml);
		}
		if (yaml instanceof List) {
			return simplifyList((List<Object>) yaml);
		}
		if (yaml instanceof OffsetDateTime) {
			return ((OffsetDateTime) yaml).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		}
		// TODO: dangerous in case of unexpected types
		return String.valueOf(yaml);
	}

	@NonNull
	protected Map<String, Object> simplifyMap(@NonNull Map<Object, Object> yaml) {
		Map<String, Object> result = new LinkedHashMap<>(yaml.size());
		yaml.forEach((k, v) -> {
			String keyStr = String.valueOf(k);
			if (result.containsKey(keyStr)) {
				throw new IllegalArgumentException("by stringifying map keys we got a key conflict with: " + keyStr);
			}
			result.put(keyStr, simplify(v));
		});
		return result;
	}

	@NonNull
	protected Map<String, Object> simplifyList(@NonNull List<Object> yaml) {
		Map<String, Object> result = new LinkedHashMap<>(yaml.size());
		ListIterator<Object> iterator = yaml.listIterator();
		while (iterator.hasNext()) {
			int index = iterator.nextIndex();
			Object simpleValue = simplify(iterator.next());
			result.put(String.valueOf(index), simpleValue);
		}
		return result;
	}

	@ToString
	@AutoService(ConfijAnyFormat.class)
	public static class YamlAnyFormat implements ConfijAnyFormat {
		@Override
		public Optional<ConfijFormat> maybeHandle(String pathTemplate) {
			if (Util.getSchemeSpecificPart(pathTemplate)
					.matches("(?i).+\\.ya?ml$")) {
				return Optional.of(new YamlFormat());
			}
			return Optional.empty();
		}
	}

	@ToString
	static class SafeConstructorWithDateTime extends SafeConstructor {
		public SafeConstructorWithDateTime() {
			super();
			this.yamlConstructors.put(Tag.TIMESTAMP, new ConstructYamlOffsetDateTime());
		}

		static class ConstructYamlOffsetDateTime extends ConstructYamlTimestamp {
			@Override
			public OffsetDateTime construct(Node node) {
				Date yamlDate = (Date) super.construct(node);
				return yamlDate.toInstant()
						.atZone(getCalendar().getTimeZone()
								.toZoneId())
						.toOffsetDateTime();
			}
		}
	}
}
