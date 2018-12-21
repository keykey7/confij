package ch.kk7.confij.source.format;

import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.NonNull;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static ch.kk7.confij.source.format.ConfijSourceFormatException.invalidFormat;

@ToString
@AutoService(ConfijSourceFormat.class)
public class YamlFormat implements ConfijSourceFormat {
	private final Yaml yaml = new Yaml(new SafeConstructor());

	@Override
	public void override(ConfijNode simpleConfig, String content) {
		final Iterable<Object> yamlIterable;
		try {
			yamlIterable = yaml.loadAll(content);
		} catch (Exception e) {
			throw invalidFormat("YAML", "cannot load from string", e);
		}
		yamlIterable.forEach(root -> {
			Object simpleRoot = simplify(root);
			ConfijNode newConfig = ConfijNode.newRootFor(simpleConfig.getConfig())
					.initializeFromMap(simpleRoot);
			simpleConfig.overrideWith(newConfig);
		});
	}

	@SuppressWarnings("unchecked")
	private Object simplify(Object yaml) {
		if (yaml == null) {
			return null;
		}
		if (yaml instanceof Map) {
			return simplifyMap((Map<Object, Object>) yaml);
		}
		if (yaml instanceof List) {
			return simplifyList((List<Object>) yaml);
		}
		if (yaml instanceof Date) {
			// note: we loose TimeZone information here. snakeyaml doesn't support OffsetDateTime
			return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)
					.format(((Date) yaml).toInstant());
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

	@Override
	public boolean canHandle(URI path) {
		return path.getSchemeSpecificPart()
				.matches("(?i).+\\.ya?ml$");
	}
}
