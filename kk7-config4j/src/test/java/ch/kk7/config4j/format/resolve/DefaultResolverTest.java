package ch.kk7.config4j.format.resolve;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultResolverTest {

	private String resolve(String template, String... x) {
		FormatSettings settings = FormatSettings.newDefaultSettings();
		ConfigFormat format = ConfigFormatMap.anyKeyMap(settings, new ConfigFormatLeaf(settings));
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < x.length; i++) {
			map.put("x" + (i + 1), x[i]);
		}
		SimpleConfig config = SimpleConfig.fromObject(map, format);
		return new DefaultResolver().resolve(config, template);
	}

	@Test
	public void resolveNone() {
		assertThat(resolve("hello"), is("hello"));
	}

	@Test
	public void resolveEmpty() {
		assertThat(resolve(""), is(""));
	}

	@Test
	public void resolveOne() {
		assertThat(resolve("hello ${x1}", "v1"), is("hello v1"));
	}

	@Test
	public void resolveTwo() {
		assertThat(resolve("hello ${x1} ${x2}", "one", "two"), is("hello one two"));
	}

	@Test
	public void resolveEmbedded() {
		assertThat(resolve("hello ${${x1}}", "x2", "yo"), is("hello yo"));
	}

	@Test
	public void resolveEmbedded2() {
		assertThat(resolve("hello ${${${x1}}}", "x2", "x3", "yo"), is("hello yo"));
	}

	@Test
	public void resolveEmbeddedCircular() {
		assertThrows(Config4jException.class, () -> resolve("hello ${x1}", "${x1}"));
	}

	@Test
	public void resolveNested() {
		assertThat(resolve("hello ${x1}", "one ${x2}", "two"), is("hello one two"));
	}

	@Test
	public void resolveNested2() {
		assertThat(resolve("hello ${x1}", "one ${x2}", "two ${x3}", "three"), is("hello one two three"));
	}

	@Test
	public void resolveNestedCircular() {
		assertThrows(Config4jException.class, () -> resolve("hello ${x1}", "1${x2}", "2${x3}", "3${x1}"));
	}
}
