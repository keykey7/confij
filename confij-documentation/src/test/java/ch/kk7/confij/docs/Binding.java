package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.values.Base64Mapper.Base64;
import ch.kk7.confij.binding.values.Base64Mapper.Base64Decoder;
import ch.kk7.confij.binding.values.SeparatedMapper.Separated;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
				.loadFrom("mapped.yml")
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
			.loadFrom(new PropertiesSource()
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

	// tag::custom-value-mapping-interface[]
	static class ColorDecoder implements ValueMapperFactory {
		@Override
		public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
			return Optional.of(Color::decode);
		}
	}

	interface Favourites {
		@ValueMapper(ColorDecoder.class)
		Color favouriteColor();
	}
	// end::custom-value-mapping-interface[]

	@Test
	public void customValueMappingWithAnnotation() {
		Favourites favourites = ConfijBuilder.of(Favourites.class)
				.loadFrom(new PropertiesSource().with("favouriteColor", "#000000"))
				.build();
		assertThat(favourites.favouriteColor()).isEqualTo(Color.BLACK);
	}

	interface EmptyColorHolder {
		Color black();
		Color green();
	}

	@Test
	public void customValueMappingWithBuilder() {
		// tag::custom-value-mapping[]
		EmptyColorHolder colorHolder = ConfijBuilder.of(EmptyColorHolder.class)
				.bindValuesForClassWith(Color::decode, java.awt.Color.class)
				// end::custom-value-mapping[]
				.loadFrom(new PropertiesSource().with("black", "#000000")
						.with("green", "#00FF00"))
				.build();
		assertThat(colorHolder.black()).isEqualTo(Color.BLACK);
		assertThat(colorHolder.green()).isEqualTo(Color.GREEN);
	}

	// tag::base64-mapping[]
	interface Base64Encoded {
		@Base64
		byte[] base64Arr();

		@Base64(decoder = Base64Decoder.RFC4648_URLSAFE)
		List<Byte> base64List();
	}
	// end::base64-mapping[]

	@Test
	public void testBuiltinCustomMappings() {
		Base64Encoded builtInMappers = ConfijBuilder.of(Base64Encoded.class)
				.loadFrom(new PropertiesSource().with("base64Arr", "AQIDBA==")
						.with("base64List", "AQIDBA=="))
				.build();
		assertThat(builtInMappers.base64Arr()).containsExactly(1, 2, 3, 4);
		assertThat(builtInMappers.base64List()).containsExactly((byte) 1, (byte) 2, (byte) 3, (byte) 4);
	}

	// tag::separated-mapping[]
	interface SeparatedConfig {
		@Separated
		List<String> commaSeparated();

		@Separated(separator = "#")
		int[] hashSeparated();

		Set<String> usuallyAList();
	}
	// end::separated-mapping[]

	@Test
	public void separated() {
		SeparatedConfig separated = ConfijBuilder.of(SeparatedConfig.class)
				.loadFrom("separated.properties")
				.build();
		assertThat(separated.commaSeparated()).containsExactly("comma", "separated", "values");
		assertThat(separated.hashSeparated()).containsExactly(1, 2, 3);
		assertThat(separated.usuallyAList()).containsExactly("common", "list", "notation");
	}
}
