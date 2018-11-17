package ch.kk7.confij.format.resolve;

import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultResolverTest {

	private static String resolve(String template, String... x) {
		FormatSettings settings = FormatSettings.newDefaultSettings();
		ConfigFormat format = ConfigFormatMap.anyKeyMap(settings, new ConfigFormatLeaf(settings));
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < x.length; i++) {
			map.put("x" + (i + 1), x[i]);
		}
		ConfijNode config = ConfijNode.newRootFor(format)
				.initializeFromMap(map);
		return new DefaultResolver().resolve(config, template);
	}

	private static AbstractStringAssert<?> assertThat(String template, String... x) {
		return org.assertj.core.api.Assertions.assertThat(resolve(template, x));
	}

	@Test
	public void resolveStatic() {
		assertThat("hello").isEqualTo("hello");
	}

	@Test
	public void resolveEmpty() {
		assertThat("").isEqualTo("");
	}

	@Test
	public void resolveOne() {
		assertThat("hello ${x1}", "v1").isEqualTo("hello v1");
	}

	@Test
	public void resolveTwo() {
		assertThat("hello ${x1} ${x2}!", "one", "two").isEqualTo("hello one two!");
	}

	@Test
	public void resolveEmbedded() {
		assertThat("hello ${${x1}}", "x2", "yo").isEqualTo("hello yo");
	}

	@Test
	public void resolveEmbedded2() {
		assertThat("hello ${${${x1}}}", "x2", "x3", "yo").isEqualTo("hello yo");
	}

	@Test
	public void resolveEmbeddedCircular() {
		assertThrows(Config4jException.class, () -> resolve("hello ${x1}", "${x1}"));
	}

	@Test
	public void resolveNested() {
		assertThat("hello ${x1}", "one ${x2}", "two").isEqualTo("hello one two");
	}

	@Test
	public void resolveNested2() {
		assertThat("hello ${x1}", "one ${x2}", "two ${x3}", "three").isEqualTo("hello one two three");
	}

	@Test
	public void resolveNestedCircular() {
		assertThrows(Config4jException.class, () -> resolve("hello ${x1}", "1${x2}", "2${x3}", "3${x1}"));
	}

	@Test
	public void looksLikeAVariable() {
		assertThat("${x1${${x1").isEqualTo("${x1${${x1");
	}

	@Test
	public void emptyVariable() {
		assertThrows(Config4jException.class, () -> resolve("hello ${}", "1"));
	}

	@Test
	public void nonexistentVariable() {
		assertThrows(Config4jException.class, () -> resolve("hello ${x1}"));
	}

	@Test
	public void escapedVariable() {
		assertThat("hello \\${x1}", "one").isEqualTo("hello ${x1}");
	}

	@Test
	public void escapedNothing() {
		assertThat("hello \\${xxx", "one").isEqualTo("hello ${xxx");
	}

	@Test
	public void doubleEscaped() {
		assertThat("hello \\\\${x1}", "one").isEqualTo("hello \\one");
	}

	@Test
	public void unnessessaryEscape() {
		assertThat("hello \\!${x1}", "one").isEqualTo("hello !one");
	}

	@Test
	public void escapedNested() {
		assertThat("hello ${x1}", "~\\${x2}~", "two").isEqualTo("hello ~${x2}~");
	}
}
