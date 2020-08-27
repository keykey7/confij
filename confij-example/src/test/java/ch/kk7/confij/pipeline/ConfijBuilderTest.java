package ch.kk7.confij.pipeline;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.ConfijSourceException;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfijBuilderTest {
	public interface MyConfig {
		String aString();
	}

	private static void assertSourceBecomes(String source, String expectedValue) {
		assertThat(ConfijBuilder.of(MyConfig.class)
				.loadFrom(source)
				.build()
				.aString()).isEqualTo(expectedValue);
	}

	@Test
	void yamlFromClasspath() {
		assertSourceBecomes("classpath:MyConfig.yaml", "iamfromyaml");
	}

	@Test
	void propertiesFromClasspath() {
		assertSourceBecomes("classpath:MyConfig.properties", "iamfromproperties");
		assertSourceBecomes("classpath:./MyConfig.properties", "iamfromproperties");
	}

	@Test
	void fromEnvvar() throws Exception {
		SystemLambda.withEnvironmentVariable("cfgprefix_aString", "envvalue")
				.execute(() -> assertSourceBecomes("env:cfgprefix", "envvalue"));
	}

	@Test
	void fromSysprops() throws Exception {
		SystemLambda.restoreSystemProperties(() -> {
			System.setProperty("sysprefix.a.1.xxx.aString", "sysvalue");
			assertSourceBecomes("sys:sysprefix.a.1.xxx", "sysvalue");
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"unknown:whatever", "file:", ":", "#", "\0", "unknown:${env:PATH}"})
	void unknownScheme(String invalidPath) {
		ConfijBuilder<MyConfig> builder = ConfijBuilder.of(MyConfig.class)
				.loadFrom(invalidPath);
		assertThrows(ConfijSourceException.class, builder::build);
	}

	@Test
	void fromFile(@TempDir Path tempDir) throws IOException {
		Path configFile = tempDir.resolve("with spaces/FileConfig!%.yml");
		Files.createDirectory(configFile.getParent());
		Files.copy(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("MyConfig.yaml")), configFile);
		assertSourceBecomes(configFile.toAbsolutePath()
				.toString(), "iamfromyaml");
	}
}
