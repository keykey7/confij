package ch.kk7.confij.source.format;

import ch.kk7.confij.source.ConfijSourceBuilder.URIish;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class YamlFormatCanHandleTest {

	public static AbstractBooleanAssert<?> assertCanHandle(String file) {
		return assertThat(new YamlFormat().canHandle(URIish.create(file)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"x.yaml", "x.YAML", "yaml.yaml.yaml", "x..YaMl", "x.yml", "☕.yml"})
	void canHandle(String file) {
		assertCanHandle(file).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"config.properties", ".yaml", ".yaml.sh", "x.yaml$", "☕yml", "PUZZLE.YAМL"})
	void cannotHandle(String file) {
		assertCanHandle(file).isFalse();
	}


}
