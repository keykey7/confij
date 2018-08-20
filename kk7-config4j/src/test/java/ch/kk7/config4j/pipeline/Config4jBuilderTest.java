package ch.kk7.config4j.pipeline;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Config4jBuilderTest {
	public interface MyConfig {
		String aString();
	}

	private static void assertSourceBecomes(String source, String expectedValue) {
		assertThat(Config4jBuilder.of(MyConfig.class)
				.withSource(source)
				.build().aString(), is(expectedValue));
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
	public void fromSysvar() {
		System.setProperty("sysprefix.a.1.xxx.aString", "sysvalue");
		assertSourceBecomes("sys:sysprefix.a.1.xxx", "sysvalue");
	}
}
