package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.format.resolve.DefaultResolver;
import ch.kk7.config4j.source.file.ResourceSource;
import ch.kk7.config4j.source.file.format.YamlFormat;
import ch.kk7.config4j.source.file.resource.ClasspathResource;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class Config4jBuilderTest {
	public interface MyConfig {
		String aString();
	}


	@Test
	public void testMitAllesUndScharf() {
		MyConfig myConfig = Config4jBuilder.of(MyConfig.class)
				.withSource(new ResourceSource("xxx.yaml", new ClasspathResource(), new YamlFormat(), new DefaultResolver()))
				.build();
		assertThat(myConfig.aString(), is("hello"));
	}
}
