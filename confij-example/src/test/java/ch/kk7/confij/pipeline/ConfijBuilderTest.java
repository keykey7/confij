package ch.kk7.confij.pipeline;

import ch.kk7.confij.source.ConfijSourceException;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ConfijBuilderTest {
	private static AbstractStringAssert<?> assertSourceBecomes(String source, String expectedValue) {
		return assertThat(ConfijBuilder.of(MyConfig.class)
				.withSource(source)
				.build()
				.aString()).isEqualTo(expectedValue);
	}

	@Test
	public void yamlFromClasspath() {
		assertSourceBecomes("classpath:MyConfig.yaml", "iamfromyaml");
	}

	@Test
	public void propertiesFromClasspath() {
		assertSourceBecomes("classpath:MyConfig.properties", "iamfromproperties");
	}

	@Test
	public void fromEnvvar() {
		// TODO: set system env before test
		assumeTrue("envvalue".equals(System.getenv("cfgprefix_aString")));
		assertSourceBecomes("env:cfgprefix", "envvalue");
	}

	@Test
	public void fromSysprops() {
		System.setProperty("sysprefix.a.1.xxx.aString", "sysvalue");
		assertSourceBecomes("sys:sysprefix.a.1.xxx", "sysvalue");
	}

	@Test
	public void unknownScheme() {
		ConfijBuilder builder = ConfijBuilder.of(MyConfig.class)
				.withSource("unknown:whatever");
		assertThrows(ConfijSourceException.class, builder::build);
	}

	@Test
	@ExtendWith(TempDirectory.class)
	public void fromFile(@TempDir Path tempDir) throws IOException {
		Path configFile = tempDir.resolve("FileConfig.yml");
		Files.copy(ClassLoader.getSystemResourceAsStream("MyConfig.yaml"), configFile);
		assertSourceBecomes(configFile.toAbsolutePath()
				.toString(), "iamfromyaml");
	}

	public interface MyConfig {
		String aString();
	}
}
