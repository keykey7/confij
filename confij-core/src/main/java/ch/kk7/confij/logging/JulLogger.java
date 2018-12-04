package ch.kk7.confij.logging;

import static ch.kk7.confij.logging.LogUtil.formatLogSupplier;
import static ch.kk7.confij.logging.LogUtil.throwableOrNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.kk7.confij.common.ServiceLoaderPriority;
import com.google.auto.service.AutoService;
import lombok.Value;

/**
 * A logger which uses java.util.logging underneath, but allows for slf4j-like log message patterns.
 * The only reason being that jul doesn't require a dependency: don't use it otherwise.
 */
@Value
public class JulLogger implements ConfijLogger {
	private final Logger logger;

	@Override
	public void debug(String message, Object... attributes) {
		log(Level.FINE, message, attributes);
	}

	@Override
	public void info(String message, Object... attributes) {
		log(Level.INFO, message, attributes);
	}

	@Override
	public void error(String message, Object... attributes) {
		log(Level.SEVERE, message, attributes);
	}

	protected void log(Level level, String message, Object... attributes) {
		logger.log(level, throwableOrNull(attributes), formatLogSupplier(message, attributes));
	}

	@AutoService(ConfijLogFactory.class)
	public static class JulLogFactory implements ConfijLogFactory, ServiceLoaderPriority {
		@Override
		public ConfijLogger getLogger(String name) {
			return new JulLogger(Logger.getLogger(name));
		}

		@Override
		public int getPriority() {
			return Integer.MIN_VALUE;
		}
	}
}
