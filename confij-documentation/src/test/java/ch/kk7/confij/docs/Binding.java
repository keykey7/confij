package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.binding.leaf.mapper.Base64Mapper.Base64;
import ch.kk7.confij.pipeline.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Binding extends DocTestBase {

	// tag::builtinMapping[]
	interface MappedConfig {
		String aString();

		Double pi(); // <1>

		@Key("int") // <2>
		int anInt();

		int[] intArray(); // <3>

		@Base64 // <4>
		byte[] base64();

		Duration duration(); // <5>
	}
	// end::builtinMapping[]

	@Test
	public void valueMapping() {
		MappedConfig dbConfig = ConfijBuilder.of(MappedConfig.class)
				.withSource("mapped.yml")
				.build();
		assertThat(dbConfig.aString()).isEqualTo("a value");
		assertThat(dbConfig.pi()).isCloseTo(3.141, withinPercentage(99));
		assertThat(dbConfig.anInt()).isEqualTo(42);
		assertThat(dbConfig.intArray()).containsExactly(1, 2, 3);
		assertThat(dbConfig.base64()).containsExactly(1, 2, 3, 4);
		assertThat(dbConfig.duration()).isEqualByComparingTo(Duration.ofNanos(7));
	}

	// tag::nested[]
	interface ServerConfig {
		Timing timing();
		URL url();
	}

	interface Timing {
		Duration keepAlive();
		Duration timeout();
	}
	// end::nested[]

	// tag::nestedList[]
	interface DbConfig {
		List<ServerConfig> dbConnections();
		Map<String, String> additionalParameters();
		byte[] privateKey();
		Nested<Timing> defaultTiming();
	}

	interface Nested<T> {
		boolean active();
		T wrapped();
	}
	// end::nestedList[]


	@Test
	public void nested() {
		// tag::nestedBuild[]
		DbConfig dbConfig = ConfijBuilder.of(DbConfig.class)
				.withSource(new PropertiesSource()
						.with("dbConnections.0.url", "https://db0.example.com")
						.with("dbConnections.0.timing.keepAlive", "30s")
						.with("additionalParameters.somekey", "somevalue"))
				.build();
		// end::nestedBuild[]
		assertThat(dbConfig.dbConnections()
				.get(0)
				.timing()
				.keepAlive()).isEqualByComparingTo(Duration.ofSeconds(30));
		assertThat(dbConfig.additionalParameters()).contains(entry("somekey", "somevalue"));
	}
}
