package ch.kk7.confij.source.format;

import ch.kk7.confij.tree.ConfijNode;
import com.google.auto.service.AutoService;
import com.typesafe.config.ConfigFactory;
import lombok.ToString;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@ToString
@AutoService(ConfijSourceFormat.class)
public class HoconResourceProvider implements ConfijSourceFormat {
	@Override
	public void override(ConfijNode rootNode, String configAsStr) {
		Object simpleRoot = simplify(ConfigFactory.parseString(configAsStr)
				.root()
				.unwrapped());
		ConfijNode newConfig = ConfijNode.newRootFor(rootNode.getConfig())
				.initializeFromMap(simpleRoot);
		rootNode.overrideWith(newConfig);
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

	@Override
	public boolean canHandle(URI path) {
		return path.getSchemeSpecificPart()
				.matches("(?s).+\\.(json|hocon)$");
	}
}
