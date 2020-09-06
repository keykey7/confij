package ch.kk7.confij.source.format;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.any.ConfijAnyFormat;
import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import com.typesafe.config.ConfigFactory;
import lombok.ToString;
import lombok.Value;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

@Value
public class HoconFormat implements ConfijFormat {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(HoconFormat.class);

	@ToString
	@AutoService(ConfijAnyFormat.class)
	public static class HoconAnyFormat implements ConfijAnyFormat {
		@Override
		public Optional<ConfijFormat> maybeHandle(String pathTemplate) {
			if (Util.getSchemeSpecificPart(pathTemplate)
					.matches("(?s).+\\.(json|hocon|conf)$")) {
				return Optional.of(new HoconFormat());
			}
			return Optional.empty();
		}
	}

	@Override
	public void override(ConfijNode rootNode, String configAsStr) {
		Object simpleRoot = parse(configAsStr);
		ConfijNode newConfig = ConfijNode.newRootFor(rootNode.getConfig())
				.initializeFromMap(simpleRoot);
		rootNode.overrideWith(newConfig);
	}

	protected Object parse(String configAsStr) {
		final Object unwrapped;
		try {
			unwrapped = ConfigFactory.parseString(configAsStr)
					.resolve()
					.root()
					.unwrapped();
		} catch (Exception e) {
			LOGGER.debug("parsing HOCON failed for string:\n{}", configAsStr);
			throw new ConfijSourceException("invalid HOCON format", e);
		}
		return simplify(unwrapped);
	}

	@SuppressWarnings("unchecked")
	protected Object simplify(Object hocon) {
		if (hocon == null) {
			return null;
		}
		if (hocon instanceof String) {
			return hocon;
		}
		if (hocon instanceof Number || hocon instanceof Boolean) {
			return String.valueOf(hocon);
		}
		if (hocon instanceof Map) {
			return simplifyMap((Map<String, Object>) hocon);
		}
		if (hocon instanceof List) {
			return simplifyList((List<Object>) hocon);
		}
		throw new IllegalStateException("encountered invalid type while simplyfying HOCON");
	}

	protected Map<String, Object> simplifyMap(Map<String, Object> hocon) {
		Map<String, Object> result = new HashMap<>(hocon.size());
		hocon.forEach((k, v) -> result.put(k, simplify(v)));
		return result;
	}

	protected Map<String, Object> simplifyList(List<Object> hocon) {
		Map<String, Object> result = new LinkedHashMap<>(hocon.size());
		ListIterator<Object> iterator = hocon.listIterator();
		while (iterator.hasNext()) {
			int index = iterator.nextIndex();
			Object simpleValue = simplify(iterator.next());
			result.put(String.valueOf(index), simpleValue);
		}
		return result;
	}
}
