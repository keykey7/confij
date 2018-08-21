package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.source.Config4jSourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Config4jBuilderTest {
	public interface MyConfig {
		String aString();
	}

	private static void assertSourceBecomes(String source, String expectedValue) {
		assertThat(Config4jBuilder.of(MyConfig.class)
				.withSource(source)
				.build()
				.aString(), is(expectedValue));
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
		Config4jBuilder builder = Config4jBuilder.of(MyConfig.class)
				.withSource("unknown:whatever");
		assertThrows(Config4jSourceException.class, builder::build);
	}

	@Test
	@ExtendWith(TempDirectory.class)
	public void fromFile(@TempDir Path tempDir) throws IOException {
		Path configFile = tempDir.resolve("FileConfig.yml");
		Files.write(configFile, ClassLoader.getSystemResourceAsStream("MyConfig.yaml")
				.readAllBytes());
		assertSourceBecomes(configFile.toAbsolutePath()
				.toString(), "iamfromyaml");
	}
}
