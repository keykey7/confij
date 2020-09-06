package ch.kk7.confij.docs;

import ch.kk7.confij.source.resource.ClasspathResource;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

public abstract class DocTestBase implements WithAssertions {
	@BeforeAll
	public static void assertTestHome() {
		String expectedTestHome = "/test/home";
		Assertions.assertThat(new File("").getAbsolutePath())
				.as("the test's working directory is expected to end with *" + expectedTestHome)
				.endsWith(expectedTestHome);
	}

	public static String classpath(String file) {
		return ClasspathResource.ofName(file)
				.read(x -> x)
				.findAny()
				.orElseThrow(IllegalStateException::new);
	}
}
