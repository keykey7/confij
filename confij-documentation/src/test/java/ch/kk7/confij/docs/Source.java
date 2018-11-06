package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.pipeline.ConfijBuilder;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Source extends DocTestBase {

	// tag::interface[]
	interface ServerConfig {
		String name();

		URL externalUrl();

		@Default("1")
		int line();

		@Default("30s") // <1>
		Duration timeout();
	}
	// end::interface[]

	@Test
	public void pipedSources() {
		System.setProperty("app.line", "3");
		// tag::pipedsource[]
		ServerConfig serverConfig = ConfijBuilder.of(ServerConfig.class)
				.withSource("classpath:generic.properties") // <2>
				.withSource("server.properties") // <3>
				.withSource("sys:app") // <4>
				.build();
		// end::pipedsource[]
		assertThat(serverConfig.toString()).isEqualToIgnoringWhitespace(classpath("pipedsource.txt"));
	}

	// tag::defaults[]
	interface HasDefaults {
		@Default("a default value")
		String aString();

		@Default("23")
		int aNumber();

		default List<Boolean> aListOfBooleans() {
			return Arrays.asList(true, false);
		}

		default int aNumberPlus1() {
			return aNumber() + 1;
		}
	}
	// end::defaults[]

	@Test
	public void defaultValues() {
		HasDefaults defaults = ConfijBuilder.of(HasDefaults.class)
				.build();
		assertThat(defaults.aString()).isEqualTo("a default value");
		assertThat(defaults.aNumber()).isEqualTo(23);
		assertThat(defaults.aListOfBooleans()).containsExactly(true, false);
		assertThat(defaults.aNumberPlus1()).isEqualTo(24);
	}
}
