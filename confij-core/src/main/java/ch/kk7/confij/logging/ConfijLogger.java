package ch.kk7.confij.logging;

import ch.kk7.confij.common.ServiceLoaderUtil;
import lombok.NonNull;

public interface ConfijLogger {
	void debug(String message, Object... attributes);
	void info(String message, Object... attributes);
	void error(String message, Object... attributes);

	@NonNull
	static ConfijLogger getLogger(String name) {
		return ServiceLoaderUtil.requireInstancesOf(ConfijLogFactory.class)
				.get(0)
				.getLogger(name);
	}
}
