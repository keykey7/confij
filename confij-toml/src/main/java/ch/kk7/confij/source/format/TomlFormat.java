package ch.kk7.confij.source.format;

import ch.kk7.confij.source.ConfijSourceBuilder.URIish;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import lombok.ToString;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static ch.kk7.confij.source.format.ConfijSourceFormatException.invalidFormat;
import static java.util.stream.Collectors.joining;

@ToString
@AutoService(ConfijSourceFormat.class)
public class TomlFormat implements ConfijSourceFormat {
	@Override
	public void override(ConfijNode rootNode, String content) {
		TomlParseResult result;
		try {
			result = Toml.parse(content);
			if (result.hasErrors()) {
				// Using default error message.
				String errors = result.errors()
						.stream()
						.map(TomlParseError::toString)
						.collect(joining("\n"));
				throw invalidFormat("TOML", errors);
			}
		} catch (ConfijSourceFormatException e) {
			throw e;
		} catch (Exception e) {
			throw invalidFormat("TOML", "cannot load from string", e);
		}

		Map<String, Object> resultMap = transformTomlTable(result);
		ConfijNode newConfig = ConfijNode.newRootFor(rootNode.getConfig())
				.initializeFromMap(resultMap);

		rootNode.overrideWith(newConfig);
	}

	/**
	 * Tomlj only support the types which defined in {@code TomlType}.
	 * <p>
	 * 1. String.class
	 * 2. Long.class
	 * 3. Double.class
	 * 4. Boolean.class
	 * 5. OffsetDateTime.class
	 * 6. LocalDateTime.class
	 * 7. LocalDate.class
	 * 8. LocalTime.class
	 * 9. TomlArray.class
	 * 10. TomlTable.class
	 */
	private Object transform(Object object) {
		if (object instanceof String) {
			return object;
		}
		if (object instanceof Long ||
				object instanceof Double ||
				object instanceof Boolean ||
				object instanceof LocalDate ||
				object instanceof LocalTime ||
				object instanceof LocalDateTime ||
				object instanceof OffsetDateTime) {
			return object.toString();
		}
		if (object instanceof TomlTable) {
			return transformTomlTable((TomlTable) object);
		}
		if (object instanceof TomlArray) {
			return transformTomlArray((TomlArray) object);
		}
		throw new IllegalArgumentException("This is a invalid type " + object.getClass() + " while parsing TOML.");
	}

	private Map<String, Object> transformTomlTable(TomlTable table) {
		Set<String> keys = table.keySet();
		Map<String, Object> result = new LinkedHashMap<>(keys.size());

		keys.forEach(key -> {
			Object object = table.get(key);
			result.put(key, transform(object));
		});

		return result;
	}

	private Map<String, Object> transformTomlArray(TomlArray array) {
		int size = array.size();
		Map<String, Object> result = new LinkedHashMap<>(size);
		for (int i = 0; i < size; i++) {
			Object item = array.get(i);
			result.put(String.valueOf(i), transform(item));
		}

		return result;
	}

	@Override
	public boolean canHandle(URIish path) {
		return path.getSchemeSpecificPart()
				.matches("(?i).+\\.toml$");
	}
}
