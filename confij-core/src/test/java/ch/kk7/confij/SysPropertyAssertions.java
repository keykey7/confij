package ch.kk7.confij;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public interface SysPropertyAssertions {
	default <T> T withSysProperty(Supplier<T> supplier, String... overrides) {
		Map<String, String> override = new HashMap<>();
		for (int i = 0; i < overrides.length; i+=2) {
			override.put(overrides[i], overrides[i + 1]);
		}
		return withSysProperty(supplier, override);
	}

	default <T> T withSysProperty(Supplier<T> supplier, Map<String, String> override) {
		Properties sysProperties = System.getProperties(); // a reference, not a copy!
		Properties clone = new Properties();
		clone.putAll(sysProperties);

		override.forEach((k, v) -> {
			if (v == null) {
				sysProperties.remove(k);
			} else {
				sysProperties.put(k, v);
			}
		});
		try {
			return supplier.get();
		} finally {
			// restore settings
			sysProperties.clear();
			sysProperties.putAll(clone);
		}
	}
}
