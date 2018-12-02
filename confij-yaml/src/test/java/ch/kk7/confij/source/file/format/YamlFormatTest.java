package ch.kk7.confij.source.file.format;

import ch.kk7.confij.ConfijBuilder;
import com.fasterxml.classmate.GenericType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
	public void integers() {
		assertThat(types.get("integers")
				.values()).allSatisfy(s -> assertThat(s).isEqualTo("12345"));
	}

	@Test
	public void floats() {
		Map<String, String> floats = types.get("floats");
		assertThat(floats.get("exponential")).isEqualTo("1230.15");
		assertThat(floats.get("fixed")).isEqualTo("1230.15");
		assertThat(floats.get("infinite negative")).isEqualTo("-Infinity");
		assertThat(floats.get("not a number")).isEqualTo("NaN");
	}

	@Test
	public void dates() {
		Map<String, String> dates = types.get("dates");
		assertThat(dates.get("canonical")).isEqualTo("2001-12-15T02:59:43.1Z");
		assertThat(dates.get("iso8601")).isEqualTo("2001-12-15T02:59:43.1Z");
		assertThat(dates.get("space")).isEqualTo("2001-12-15T02:59:43.1Z");
	}

	@Test
	public void miscellaneous() {
		Map<String, String> miscellaneous = types.get("miscellaneous");
		assertThat(miscellaneous.get("null")).isEqualTo(null);
		assertThat(miscellaneous.get("null bis")).isEqualTo(null);
		assertThat(miscellaneous.get("true ter")).isEqualTo("true");
		assertThat(miscellaneous.get("false bis")).isEqualTo("false");
	}

	@Test
	public void multidoc() {
		Multidoc multidoc = ConfijBuilder.of(Multidoc.class)
				.loadFrom("classpath:multidoc.yml")
				.build();
		assertThat(multidoc.x()).isEqualTo(1);
		assertThat(multidoc.y()).isEqualTo(2);
		assertThat(multidoc.z()).isEqualTo(2);
	}
}
