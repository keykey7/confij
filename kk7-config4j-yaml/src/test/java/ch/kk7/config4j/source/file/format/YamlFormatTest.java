package ch.kk7.config4j.source.file.format;

import ch.kk7.config4j.pipeline.Config4jBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFormatTest {
	interface YamlTypes extends Map<String, Map<String, String>> {
	}

	@Test
	public void x() {
		Config4jBuilder.of(YamlTypes.class)
				.withSource("classpath:types.yml")
				.build();
	}
}
