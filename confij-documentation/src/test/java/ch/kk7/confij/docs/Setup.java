package ch.kk7.confij.docs;

import ch.kk7.confij.pipeline.ConfijBuilder;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class Setup extends DocTestBase {

	// tag::simple_interface[]
	interface ServerConfig { // <1>
		String name();
		URL externalUrl();
	}
	// end::simple_interface[]

	@Test
	public void gettingStarted() {
		// tag::simple_builder[]
		ServerConfig serverConfig = ConfijBuilder.of(ServerConfig.class)
				.withSource("server.properties") // <2>
				.build(); // <3>
		// end::simple_builder[]
	}
}
