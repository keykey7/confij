package ch.kk7.confij.source.format;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

class PropertiesFormatTest implements WithAssertions {
	private PropertiesFormat propertiesFormat;

	@BeforeEach
	public void initialize() {
		propertiesFormat = new PropertiesFormat();
	}

	private static Properties props(String testString) {
		String withNewline = testString.replace('|', '\n');
		Properties properties = new Properties();
		try (StringReader r = new StringReader(withNewline)) {
			properties.load(r);
		} catch (IOException e) {
			throw new RuntimeException("failed to read testdata " + testString, e);
		}
		return properties;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> map(String testString) {
		return (Map) props(testString);
	}

	private Map<String, String> flatmapPrefixedBy(String properties, String prefix) {
		return propertiesFormat.flatmapPrefixedBy(map(properties), prefix);
	}

	@Test
	public void flatmapSoloKey() {
		assertThat(flatmapPrefixedBy("", null)).isEmpty();
		assertThat(flatmapPrefixedBy("a.b.c=value", null)).containsOnlyKeys("a.b.c")
				.containsValue("value");
		assertThat(flatmapPrefixedBy("a.b.c=value", "a")).containsOnlyKeys("b.c")
				.containsValue("value");
		assertThat(flatmapPrefixedBy("a.b.c=value", "a.b")).containsOnlyKeys("c")
				.containsValue("value");
	}

	@Test
	public void flatmapIgnoringUnknownKeys() {
		assertThat(flatmapPrefixedBy("", "a")).isEmpty();
		assertThat(flatmapPrefixedBy("a.b.c=value", "b")).isEmpty();
		assertThat(flatmapPrefixedBy("fuu=bar|a.b.c=value", "a")).containsOnlyKeys("b.c");
	}

	@Test
	public void flatmapMultiKey() {
		String testString = "a.b.c=value|a.x.y=value2|fuu=bar";
		assertThat(flatmapPrefixedBy(testString, null)).containsOnlyKeys("a.b.c", "a.x.y", "fuu");
		assertThat(flatmapPrefixedBy(testString, "a")).containsOnlyKeys("b.c", "x.y");
		assertThat(flatmapPrefixedBy(testString, "a.b")).containsOnlyKeys("c");
	}
}
