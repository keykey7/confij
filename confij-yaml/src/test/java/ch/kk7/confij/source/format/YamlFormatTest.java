package ch.kk7.confij.source.format;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.common.GenericType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class YamlFormatTest {
	private static Map<String, Map<String, String>> types;

	private interface Multidoc {
		int x();

		int y();

		int z();
	}

	@BeforeAll
	public static void setupTypes() {
		types = ConfijBuilder.of(new GenericType<Map<String, Map<String, String>>>() {
		})
				.loadFrom("classpath:types.yml")
				.build();
	}

	@Test
	void integers() {
		assertThat(types.get("integers")
				.values()).allSatisfy(s -> assertThat(s).isEqualTo("12345"));
	}

	@Test
	void floats() {
		assertThat(types.get("floats")).containsEntry("exponential", "1230.15")
				.containsEntry("fixed", "1230.15")
				.containsEntry("infinite negative", "-Infinity")
				.containsEntry("not a number", "NaN");
	}

	@Test
	void dates() {
		assertThat(types.get("dates")).containsEntry("canonical", "2001-12-15T02:59:43.1Z")
				.containsEntry("iso8601", "2001-12-14T21:59:43.1-05:00")
				.containsEntry("space", "2001-12-14T21:59:43.1-05:00")
				.containsEntry("date", "2002-12-14T00:00:00Z");
	}

	@Test
	void datesAtDifferentLocalTz() {
		runWithSystemTimezone(TimeZone.getTimeZone(ZoneId.of("CET")), this::dates);
		runWithSystemTimezone(TimeZone.getTimeZone(ZoneId.of("UTC")), this::dates);
	}

	@Test
	void miscellaneous() {
		assertThat(types.get("miscellaneous")).containsEntry("null", null)
				.containsEntry("null bis", null)
				.containsEntry("true ter", "true")
				.containsEntry("false bis", "false");
	}

	@Test
	void multidoc() {
		Multidoc multidoc = ConfijBuilder.of(Multidoc.class)
				.loadFrom("classpath:multidoc.yml")
				.build();
		assertThat(multidoc.x()).isEqualTo(1);
		assertThat(multidoc.y()).isEqualTo(2);
		assertThat(multidoc.z()).isEqualTo(2);
	}

	private static void runWithSystemTimezone(TimeZone timeZone, Runnable test) {
		final TimeZone original = TimeZone.getDefault();
		try {
			TimeZone.setDefault(timeZone);
			test.run();
		} finally {
			TimeZone.setDefault(original);
		}
	}
}
