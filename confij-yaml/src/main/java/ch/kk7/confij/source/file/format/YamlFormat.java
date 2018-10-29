package ch.kk7.confij.source.file.format;

import ch.kk7.confij.source.simple.ConfijNode;
import com.google.auto.service.AutoService;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.kk7.confij.source.file.format.FormatParsingException.invalidFormat;

@AutoService(ResourceFormat.class)
public class YamlFormat implements ResourceFormat {
	private final Yaml yaml = new Yaml(new SafeConstructor());

	@Override
	public void override(ConfijNode simpleConfig, String content) {
		final Object root;
		// TODO: support multiple yaml in one file
		try {
			root = yaml.load(content);
		} catch (Exception e) {
			throw invalidFormat("YAML", "content cannot be parsed:\n{}", content, e);
		}
		Object simpleRoot = simplify(root);
		ConfijNode newConfig = ConfijNode.newRootFor(simpleConfig.getConfig())
				.initializeFromMap(simpleRoot);
		simpleConfig.overrideWith(newConfig);
	}

	@SuppressWarnings("unchecked")
	private Object simplify(Object yaml) {
		if (yaml instanceof Map) {
			// simplify keys
			Map<Object, Object> objMap = (Map<Object, Object>) yaml;
			new HashSet<>(objMap.keySet()).forEach(key -> {
				String keyStr = Objects.toString(key);
				if (!keyStr.equals(key)) {
					if (objMap.containsKey(keyStr)) {
						throw new IllegalArgumentException("by stringifying map keys we got a key conflict with: " + keyStr);
					}
					objMap.put(keyStr, objMap.remove(key));
				}
			});
			objMap.replaceAll((key, value) -> simplify(value));
		} else if (yaml instanceof List) {
			((List<Object>) yaml).replaceAll(this::simplify);
		} else if (yaml instanceof Date) {
			yaml = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)
					.format(((Date) yaml).toInstant());
		} else if (yaml != null) {
			yaml = Objects.toString(yaml);
		}
		return yaml;
	}

	@Override
	public boolean canHandle(URI path) {
		return path.getSchemeSpecificPart()
				.matches("(?i).+\\.ya?ml$");
	}
}
