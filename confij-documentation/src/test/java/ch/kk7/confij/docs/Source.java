package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.common.ServiceLoaderPriority;
import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.source.env.SystemPropertiesSource;
import ch.kk7.confij.source.format.PropertiesFormat;
import ch.kk7.confij.source.resource.ClasspathResource;
import ch.kk7.confij.source.resource.ConfijResource;
import ch.kk7.confij.source.resource.ConfijResource.ResourceContent;
import ch.kk7.confij.source.resource.FileResource;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.auto.service.AutoService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

class Source extends DocTestBase {
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
	void pipedSources() throws Exception {
		SystemLambda.restoreSystemProperties(() -> {
			System.setProperty("app.line", "3");
			// tag::pipedsource[]
			ServerConfig serverConfig = ConfijBuilder.of(ServerConfig.class)
					.loadFrom("classpath:generic.properties") // <2>
					.loadFrom("server.properties") // <3>
					.loadFrom("sys:app") // <4>
					.build();
			// end::pipedsource[]
			assertThat(serverConfig.toString()).isEqualToIgnoringWhitespace(classpath("pipedsource.txt"));
		});
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
	void defaultValues() {
		HasDefaults defaults = ConfijBuilder.of(HasDefaults.class)
				.build();
		assertThat(defaults.aString()).isEqualTo("a default value");
		assertThat(defaults.aNumber()).isEqualTo(23);
		assertThat(defaults.aListOfBooleans()).containsExactly(true, false);
		assertThat(defaults.aNumberPlus1()).isEqualTo(24);
	}

	// tag::nestedinterface[]
	interface Config {
		String key();
		Nested nest(); // <1>
		List<Nested> listOfNest(); // <2>
		Map<String, Nested> mapOfNest(); // <3>
	}

	interface Nested {
		int x();
		int y();
	}
	// end::nestedinterface[]

	@Test
	void nestedPropertiesFile() {
		Config config = ConfijBuilder.of(Config.class)
				.loadFrom("nested.properties")
				.build();
		assertThat(config.key()).isEqualTo("value");
		assertThat(config.nest()
				.x()).isEqualTo(0);
		assertThat(config.nest()
				.y()).isEqualTo(1);
		assertThat(config.listOfNest()).hasSize(3);
		assertThat(config.mapOfNest()).containsOnlyKeys("mykey", "myotherkey")
				.hasEntrySatisfying("myotherkey", nested -> assertThat(nested.y()).isEqualTo(0));
	}

	@Test
	void powerOfTheAnySource() {
		System.setProperty("some.prefix.key", "fromSysprops");
		// tag::anysource[]
		ConfijBuilder.of(Config.class)
				.loadFrom("nested.properties") // properties file in current working directory
				.loadFrom(new File("nested.properties").getAbsolutePath()) // equivalent
				.loadFrom("file:nested.properties") // equivalent
				.loadFrom("classpath:nested${key}.yaml") // a YAML file on the classpath root
				.loadFrom("${key}:nestedvalue.yaml") // ...with variable replacements
				.loadFrom("sys:some.prefix") // from system properties
				.loadFrom("env:some_prefix") // from environment variables
				.build();
		// end::anysource[]
	}

	// tag::envvarsyspropsource[]
	interface Configs {
		String fromEnvvar();

		String fromSysprop();

		@Default("${env:HOME}")
		Path myHome();

		@Default("${sys:file.separator}")
		String fileSep();
	}
	// end::envvarsyspropsource[]

	@Test
	void envvarsAndSyspropSource() throws Exception {
		String value1 = UUID.randomUUID() + "";
		String value2 = UUID.randomUUID() + "";
		/*
		// tag::set-envvarsyspropsource[]
		> export A_PREFIX_fromEnvvar='some value'
		> java -Danother.prefix.fromSysprop="another value" ...
		// end::set-envvarsyspropsource[]
		*/
		SystemLambda.withEnvironmentVariable("A_PREFIX_fromEnvvar", value1)
				.execute(() -> {
					SystemLambda.restoreSystemProperties(() -> {
						System.setProperty("another.prefix.fromSysprop", value2);
						Configs configs = ConfijBuilder.of(Configs.class)
								.loadFrom("env:A_PREFIX")
								.loadFrom("sys:another.prefix")
								.build();
						assertThat(configs.fromEnvvar()).isEqualTo(value1);
						assertThat(configs.fromSysprop()).isEqualTo(value2);
						assertThat(configs.myHome()).isEqualTo(Paths.get(System.getenv("HOME")));
						assertThat(configs.fileSep()).isEqualTo(File.separator);
					});
				});
	}

	// tag::yaml-interface[]
	interface ComplexYaml {
		List<String> listOfStrings();
		Map<String, Integer> mapOfIntegers();
		Map<String, Integer> mapOfIntegersClone();
		LinkedList<String> anotherList();
		OffsetDateTime date();
		@Key("true") boolean isTrue();
		@Key("false") boolean isFalse();
	}
	// end::yaml-interface[]

	@Test
	void complexYaml() {
		ComplexYaml yaml = ConfijBuilder.of(ComplexYaml.class)
				.loadFrom("complex.yaml")
				.build();
		assertThat(yaml.listOfStrings()).containsExactly("String", "String on a single line", "Double quotation marks\t");
		assertThat(yaml.mapOfIntegers()).allSatisfy((s, i) -> assertThat(i).isEqualTo(12345));
		assertThat(yaml.mapOfIntegersClone()).isEqualTo(yaml.mapOfIntegers());
		assertThat(yaml.anotherList()).containsExactly(null, "big");
		// TODO: this is only a limitation of the yaml lib used. not really intentional behavior
		assertThat(yaml.date()).isEqualTo(OffsetDateTime.parse("2001-12-14t21:59:43.10-05:00")
				.atZoneSameInstant(ZoneOffset.UTC.normalized())
				.toOffsetDateTime());
		assertThat(yaml.isTrue()).isTrue();
		assertThat(yaml.isFalse()).isFalse();
	}

	@AutoService(ConfijAnyResource.class)
	// tag::resourceprovider-service-ignored[]
	public static class AnUnimportantFoo extends FooResource implements ServiceLoaderPriority {
		@Override
		public int getPriority() {
			return ServiceLoaderPriority.DEFAULT_PRIORITY - 1000;
		}

		@Override
		public Optional<ConfijResource> maybeHandle(String path) {
			return Optional.of((ConfijResource) resolver -> Stream.of(new ResourceContent("foo=OTHER", path)))
					.filter(__ -> path.startsWith("foo:"));
		}
	}
	// end::resourceprovider-service-ignored[]

	@AutoService(ConfijAnyResource.class)
	// tag::resourceprovider-service[]
	// +file: META-INF/services/ch.kk7.confij.source.file.resource.ConfijAnyResource
	public static class FooResource implements ConfijAnyResource {
		@Override
		public Optional<ConfijResource> maybeHandle(String path) {
			return Optional.of((ConfijResource) resolver -> Stream.of(new ResourceContent("foo=bar", path)))
					.filter(__ -> path.startsWith("foo:"));
		}
	}

	interface Foo {
		String foo();
	}
	// end::resourceprovider-service[]

	@Test
	void customResourceProvider() {
		// tag::resourceprovider[]
		Foo foo = ConfijBuilder.of(Foo.class)
				.loadFrom("foo:fuuuuu.properties")
				.build();
		// end::resourceprovider[]
		assertThat(foo.foo()).isEqualTo("bar");
		assertThat(ServiceLoaderUtil.requireInstancesOf(ConfijAnyResource.class)).anySatisfy(
				x -> assertThat(x).isInstanceOf(AnUnimportantFoo.class));
	}

	@Test
	void explicitSources() throws Exception {
		SystemLambda.restoreSystemProperties(() -> {
			System.setProperty("app.line", "3");
			// tag::pipedsource-explicit[]
			ServerConfig serverConfig = ConfijBuilder.of(ServerConfig.class)
					.loadFrom(ClasspathResource.ofName("generic.properties"))
					.loadFrom(FileResource.ofFile("server.properties"), PropertiesFormat.withoutPrefix())
					.loadFrom(SystemPropertiesSource.withPrefix("app"))
					.build();
			// end::pipedsource-explicit[]
			assertThat(serverConfig.toString()).isEqualToIgnoringWhitespace(classpath("pipedsource.txt"));
		});
	}
}
