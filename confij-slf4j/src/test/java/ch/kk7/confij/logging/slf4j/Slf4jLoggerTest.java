package ch.kk7.confij.logging.slf4j;

import ch.kk7.confij.logging.ConfijLogger;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLoggerFactoryExtension.class)
class Slf4jLoggerTest implements WithAssertions {
	TestLogger slf4j = TestLoggerFactory.getTestLogger(Slf4jLoggerTest.class);

	@Test
	void testLoggerItself() {
		ConfijLogger logger = ConfijLogger.getLogger(Slf4jLoggerTest.class.getName());
		logger.debug("a debug msg");
		logger.info("a info msg");
		logger.error("a error msg");
		assertThat(slf4j.getAllLoggingEvents()).containsOnly(LoggingEvent.debug("a debug msg"),
				LoggingEvent.info("a info msg"),
				LoggingEvent.error("a error msg"));
	}
}
