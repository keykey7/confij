package ch.kk7.confij.docs;

import ch.kk7.confij.pipeline.ConfijBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class Docs {

	// TODO: cd into testfolder like
	// https://stackoverflow.com/questions/840190/changing-the-current-working-directory-in-java/8204584#8204584
	// ...or assert we are in the correct one

	@BeforeAll
	public static void assertTestHome() {
		String expectedTestHome = "/test/home";
		Assertions.assertThat(new File("").getAbsolutePath())
				.as("the test's working directory is expected to end with *" + expectedTestHome)
				.endsWith(expectedTestHome);
	}

	// tag::simple_interface[]
	interface ServerConfig {
		String name();
		URL externalUrl();
	}
	// end::simple_interface[]

	@Test
	public void gettingStarted() {
		// tag::simple_builder[]
		ServerConfig serverConfig = ConfijBuilder.of(ServerConfig.class)
				.withSource("server.properties")
				.build();
		// end::simple_builder[]
	}
}
