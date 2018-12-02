package ch.kk7.confij.template;

import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionLeaf;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.ConfijNode;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class DefaultResolverTest implements WithAssertions {

	private static String resolve(String template, String... x) {
		NodeBindingContext settings = NodeBindingContext.newDefaultSettings();
		NodeDefinition format = NodeDefinitionMap.anyKeyMap(settings, new NodeDefinitionLeaf(settings));
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < x.length; i++) {
			map.put("x" + (i + 1), x[i]);
		}
		ConfijNode config = ConfijNode.newRootFor(format)
				.initializeFromMap(map);
		return new DefaultResolver().resolveValue(config, template);
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
		assertThatThrownBy(() -> resolve("hello ${x1}", "${x1}")).isInstanceOf(ConfijException.class);
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
		assertThatThrownBy(() -> resolve("hello ${x1}", "1${x2}", "2${x3}", "3${x1}")).isInstanceOf(ConfijException.class);
	}

	@Test
	public void looksLikeAVariable() {
		assertThat("${x1${${x1").isEqualTo("${x1${${x1");
	}

	@Test
	public void emptyVariable() {
		assertThatThrownBy(() -> resolve("hello ${}", "1")).isInstanceOf(ConfijException.class);
	}

	@Test
	public void nonexistentVariable() {
		assertThatThrownBy(() -> resolve("hello ${x1}")).isInstanceOf(ConfijException.class);
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
	public void currency() {
		assertThat("23$ 10¢").isEqualTo("23$ 10¢");
	}

	@Test
	public void noNullReferences() {
		assertThatThrownBy(() -> resolve("${x1}", (String) null)).isInstanceOf(ConfijException.class);
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
