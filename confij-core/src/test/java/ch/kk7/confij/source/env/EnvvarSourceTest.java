package ch.kk7.confij.source.env;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.common.GenericType;
import ch.kk7.confij.source.ConfijSourceException;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.UUID;

class EnvvarSourceTest implements WithAssertions {
	interface Config {
		String xy();

		Map<String, String> others();
	}

	@Test
	public void withPrefix() throws Exception {
		String value1 = UUID.randomUUID() + "";
		String value2 = UUID.randomUUID() + "";
		SystemLambda.withEnvironmentVariable("A_PREFIX_xy", value1)
				.and("A_PREFIX_others_fuu.", value2)
				.and("A_PREFIXORNORATALL_xy", "fuuuuu")
				.execute(() -> {
					Config config = ConfijBuilder.of(Config.class)
							.loadFrom("env:A_PREFIX")
							.build();
					assertThat(config.xy()).isEqualTo(value1);
					assertThat(config.others()).containsExactly(new SimpleEntry<>("fuu.", value2));
				});
	}

	@Test
	public void emptyStringEnvvar() throws Exception {
		String value1 = UUID.randomUUID() + "";
		SystemLambda.withEnvironmentVariable("A_PREFIX_others_", value1)
				.and("_", "whatever")
				.execute(() -> {
					Config config = ConfijBuilder.of(Config.class)
							.loadFrom("env:A_PREFIX")
							.build();
					assertThat(config.others()).containsEntry("", value1);
				});
	}

	@ParameterizedTest
	@ValueSource(strings = {"PRE_", "PRE_xy_", "PRE__", "PRE_others_cannot_map_this", "PRE_others__", "PRE_xy_invalid", "PRE_others"})
	public void keysCannotBindToConfigStructure(String envvar) throws Exception {
		SystemLambda.withEnvironmentVariable(envvar, "whatever")
				.execute(() -> {
					assertThatThrownBy(() -> ConfijBuilder.of(Config.class)
							.loadFrom("env:PRE")
							.build()).isInstanceOf(ConfijBindingException.class);
				});
	}

	@Test
	public void keyConflictsAreSourceIssues() throws Exception {
		SystemLambda.withEnvironmentVariable("PRE_a_b", "whatever")
				.and("PRE_a", "whatever")
				.execute(() -> assertThatThrownBy(() -> ConfijBuilder.of(new GenericType<Map<String, String>>() {
				})
						.loadFrom("env:PRE")
						.build()).isInstanceOf(ConfijSourceException.class)
						.hasMessageContaining("PRE_a"));
	}

}
