package ch.kk7.confij.template;

import ch.kk7.confij.SysPropertyAssertions;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionLeaf;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class SimpleVariableResolverTest implements WithAssertions, SysPropertyAssertions {

	private static String resolve(String template, String... x) {
		NodeBindingContext settings = NodeBindingContext.newDefaultSettings(new SimpleVariableResolver());
		NodeDefinition format = NodeDefinitionMap.anyKeyMap(settings, new NodeDefinitionLeaf(settings));
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < x.length; i++) {
			map.put("x" + (i + 1), x[i]);
		}
		ConfijNode config = ConfijNode.newRootFor(format)
				.initializeFromMap(map);
		return new SimpleVariableResolver().resolveValue(config, template);
	}

	private static AbstractStringAssert<?> assertResolve(String template, String... x) {
		return org.assertj.core.api.Assertions.assertThat(resolve(template, x));
	}

	@Test
	void resolveStatic() {
		assertResolve("hello").isEqualTo("hello");
	}

	@Test
	void resolveEmpty() {
		assertResolve("").isEqualTo("");
	}

	@Test
	void resolveOne() {
		assertResolve("hello ${x1}", "v1").isEqualTo("hello v1");
	}

	@Test
	void resolveTwo() {
		assertResolve("hello ${x1} ${x2}!", "one", "two").isEqualTo("hello one two!");
	}

	@Test
	void resolveEmbedded() {
		assertResolve("hello ${${x1}}", "x2", "yo").isEqualTo("hello yo");
	}

	@Test
	void resolveEmbedded2() {
		assertResolve("hello ${${${x1}}}", "x2", "x3", "yo").isEqualTo("hello yo");
	}

	@Test
	void resolveEmbeddedCircular() {
		assertThatThrownBy(() -> resolve("hello ${x1}", "${x1}")).isInstanceOf(ConfijException.class);
	}

	@Test
	void resolveNested() {
		assertResolve("hello ${x1}", "one ${x2}", "two").isEqualTo("hello one two");
	}

	@Test
	void resolveNested2() {
		assertResolve("hello ${x1}", "one ${x2}", "two ${x3}", "three").isEqualTo("hello one two three");
	}

	@Test
	void resolveNestedCircular() {
		assertThatThrownBy(() -> resolve("hello ${x1}", "1${x2}", "2${x3}", "3${x1}")).isInstanceOf(ConfijException.class);
	}

	@Test
	void looksLikeAVariable() {
		assertResolve("${x1${${x1").isEqualTo("${x1${${x1");
	}

	@Test
	void emptyVariable() {
		assertThatThrownBy(() -> resolve("hello ${}", "1")).isInstanceOf(ConfijException.class);
	}

	@Test
	void nonexistentVariable() {
		assertThatThrownBy(() -> resolve("hello ${x1}")).isInstanceOf(ConfijException.class);
	}

	@Test
	void escapedVariable() {
		assertResolve("hello \\${x1}", "one").isEqualTo("hello ${x1}");
	}

	@Test
	void escapedNothing() {
		assertResolve("hello \\${xxx", "one").isEqualTo("hello ${xxx");
	}

	@Test
	void doubleEscaped() {
		assertResolve("hello \\\\${x1}", "one").isEqualTo("hello \\one");
	}

	@Test
	void currency() {
		assertResolve("23$ 10¢").isEqualTo("23$ 10¢");
	}

	@Test
	void noNullReferences() {
		assertThatThrownBy(() -> resolve("${x1}", (String) null)).isInstanceOf(ConfijException.class);
	}

	@Test
	void unnessessaryEscape() {
		assertResolve("hello \\!${x1}", "one").isEqualTo("hello !one");
	}

	@Test
	void escapedNested() {
		assertResolve("hello ${x1}", "~\\${x2}~", "two").isEqualTo("hello ~${x2}~");
	}

	@Test
	void sysProperty() {
		withSysProperty(() -> assertResolve("hello ${sys:name}").isEqualTo("hello John"), "name", "John");
	}

	@Test
	void sysPropertyDoesNotExist() {
		withSysProperty(() -> assertThatThrownBy(() -> resolve("hello ${sys:name}")).isInstanceOf(ConfijException.class), "name", null);
	}

	@Test
	void sysPropertyEmbedded() {
		withSysProperty(() -> assertResolve("hello ${x1}", "${sys:name}").isEqualTo("hello John"), "name", "John");
	}

	@Test
	void sysPropertyHome() {
		withSysProperty(() -> assertResolve("${sys:user.home}").isEqualTo("/home/bla"), "user.home", "/home/bla");
	}
}
