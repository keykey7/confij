package ch.kk7.confij.source.format;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
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
	void flatmapSoloKey() {
		assertThat(flatmapPrefixedBy("", null)).isEmpty();
		assertThat(flatmapPrefixedBy("a.b.c=value", null)).containsOnlyKeys("a.b.c")
				.containsValue("value");
		assertThat(flatmapPrefixedBy("a.b.c=value", "a")).containsOnlyKeys("b.c")
				.containsValue("value");
		assertThat(flatmapPrefixedBy("a.b.c=value", "a.b")).containsOnlyKeys("c")
				.containsValue("value");
	}

	@Test
	void flatmapIgnoringUnknownKeys() {
		assertThat(flatmapPrefixedBy("", "a")).isEmpty();
		assertThat(flatmapPrefixedBy("a.b.c=value", "b")).isEmpty();
		assertThat(flatmapPrefixedBy("fuu=bar|a.b.c=value", "a")).containsOnlyKeys("b.c");
	}

	@Test
	void flatmapMultiKey() {
		String testString = "a.b.c=value|a.x.y=value2|fuu=bar";
		assertThat(flatmapPrefixedBy(testString, null)).containsOnlyKeys("a.b.c", "a.x.y", "fuu");
		assertThat(flatmapPrefixedBy(testString, "a")).containsOnlyKeys("b.c", "x.y");
		assertThat(flatmapPrefixedBy(testString, "a.b")).containsOnlyKeys("c");
	}

	@Test
	void bracketListFormat() {
		String testString = "values[0]=1|values[1]=2|values[2]=3";
		ListValueHolder holder = ConfijBuilder.of(ListValueHolder.class)
				.loadFrom(new PropertiesSource(props(testString)))
				.build();

		assertThat(holder.values()).containsExactly("1", "2", "3");
	}

	interface ListValueHolder {
		List<String> values();
	}
}
