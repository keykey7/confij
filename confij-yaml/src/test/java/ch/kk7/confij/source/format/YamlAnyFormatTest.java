package ch.kk7.confij.source.format;

import ch.kk7.confij.source.format.YamlFormat.YamlAnyFormat;
import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class YamlAnyFormatTest {
	public static OptionalAssert<ConfijFormat> assertCanHandle(String file) {
		return assertThat(new YamlAnyFormat().maybeHandle(file));
	}

	@ParameterizedTest
	@ValueSource(strings = {"x.yaml", "x.YAML", "yaml.yaml.yaml", "x..YaMl", "x.yml", "☕.yml"})
	void canHandle(String file) {
		assertCanHandle(file).isNotEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"config.properties", ".yaml", ".yaml.sh", "x.yaml$", "☕yml", "PUZZLE.YAМL"})
	void cannotHandle(String file) {
		assertCanHandle(file).isEmpty();
	}
}
