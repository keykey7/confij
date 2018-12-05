package ch.kk7.confij.logging;

import lombok.Getter;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class JulLoggerTest implements WithAssertions {
	private ConfijLogger logger;
	private JulHandler julHandler;

	private static class JulHandler extends Handler {
		@Getter
		private List<LogRecord> logRecords = Collections.synchronizedList(new ArrayList<>());

		@Override
		public void publish(LogRecord logRecord) {
			logRecords.add(logRecord);
		}

		@Override
		public void flush() {

		}

		@Override
		public void close() throws SecurityException {

		}
	}

	@BeforeEach
	public void injectLogHandler() {
		String loggerName = "test" + UUID.randomUUID();
		logger = ConfijLogger.getLogger(loggerName);

		julHandler = new JulHandler();
		Logger julLogger = Logger.getLogger(loggerName);
		julLogger.setUseParentHandlers(false);
		julLogger.addHandler(julHandler);
		julLogger.setLevel(Level.ALL);
	}

	@Test
	public void testLogger() {
		logger.debug("a debug msg");
		logger.info("a info msg");
		logger.error("a error msg");
		assertThat(julHandler.getLogRecords()).anySatisfy(logRecord -> assertThat(logRecord.getMessage()).isEqualTo("a debug msg"));
	}

	@Test
	public void testLoggerTemplate() {
		logger.debug("a debug msg {}", "fuu");
		assertThat(julHandler.getLogRecords()).allSatisfy(logRecord -> assertThat(logRecord.getMessage()).isEqualTo("a debug msg fuu"));
	}

	@Test
	public void testNull() {
		logger.debug(null);
		assertThat(julHandler.getLogRecords()).allSatisfy(logRecord -> assertThat(logRecord.getMessage()).isNull());
	}
}
