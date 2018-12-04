package ch.kk7.confij.logging.slf4j;

import ch.kk7.confij.logging.ConfijLogFactory;
import ch.kk7.confij.logging.ConfijLogger;
import com.google.auto.service.AutoService;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class Slf4jLogger implements ConfijLogger {

	private final Logger logger;

	@Override
	public void debug(String message, Object... attributes) {
		logger.debug(message, attributes);
	}

	@Override
	public void info(String message, Object... attributes) {
		logger.info(message, attributes);
	}

	@Override
	public void error(String message, Object... attributes) {
		logger.error(message, attributes);
	}

	@AutoService(ConfijLogFactory.class)
	public static class JulLogFactory implements ConfijLogFactory {

		@Override
		public Slf4jLogger getLogger(String name) {
			return new Slf4jLogger(LoggerFactory.getLogger(name));
		}
	}
}
