package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.annotation.VariableResolver;
import ch.kk7.confij.template.NoopValueResolver.NoopResolver;
import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import ch.kk7.confij.template.ValueResolver;import ch.kk7.confij.tree.ConfijNode;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class Templates implements WithAssertions {

	// tag::simple[]
	interface Salutation {
		@Default("John")
		String name();

		@Default("Hi ${name}")
		String hello();
	}
	// end::simple[]

	@Test
	void simple() {
		Salutation salutation = ConfijBuilder.of(Salutation.class)
				.loadFrom(new PropertiesSource().set("name", "Bob"))
				.build();
		assertThat(salutation.hello()).isEqualTo("Hi Bob");
		salutation = ConfijBuilder.of(Salutation.class)
				.loadFrom(new PropertiesSource().set("hello", "Cya"))
				.build();
		assertThat(salutation.hello()).isEqualTo("Cya");
	}

	// tag::relative-interface[]
	interface Letter {
		Salutation salutation();

		@Default("I dearly miss you, ${salutation.name}!") // <1>
		String body();

		Regards regards();
	}

	interface Regards {
		@Default("${.salutation.name}'s friend") // <2>
		String sender();

		@Default("Sincierly\n${sender}")
		String regards();
	}
	// end::relative-interface[]

	@Test
	void relative() {
		// tag::relative[]
		Letter letter = ConfijBuilder.of(Letter.class).build();
		assertThat(letter.regards().sender()).isEqualTo("John's friend");
		assertThat(letter.body()).isEqualTo("I dearly miss you, John!");
		// end::relative[]
	}

	// tag::recursive[]
	interface Recursive {
		@Default("Alice")
		String party1();
		@Default("Bob")
		String party2();
		@Default("1")
		int victimsId();
		@Default("Poor ${party${victimsId}}!")
		String victim();
	}
	// end::recursive[]

	@Test
	void recursive() {
		Recursive recursive = ConfijBuilder.of(Recursive.class).build();
		assertThat(recursive.victim()).isEqualTo("Poor Alice!");
	}

	// tag::noop[]
	interface Noop {
		@NoopResolver
		String canContainDollar();
	}
	// end::noop[]

	// tag::global-noop[]
	@NoopResolver
	interface GlobalNoop {
		String canContainDollar();
	}
	// end::global-noop[]

	interface BuilderNoop {
		String canContainDollar();
	}

	@Test
	void noop() {
		assertThat(ConfijBuilder.of(Noop.class)
				.loadFrom(new PropertiesSource().set("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");
		assertThat(ConfijBuilder.of(GlobalNoop.class)
				.loadFrom(new PropertiesSource().set("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");

		assertThat(
				// tag::builder-noop[]
		ConfijBuilder.of(BuilderNoop.class).templatingDisabled()
				// end::builder-noop[]
				.loadFrom(new PropertiesSource().set("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");
	}

	// tag::customresolver[]
	static class FooResolver implements ValueResolver {
		@Override
		public String resolveValue(ConfijNode baseLeaf, String valueToResolve) {
			return "foo";
		}
	}

	interface CustomResolver {
		@Default("everything becomes foo")
		@VariableResolver(FooResolver.class)
		String everyVariableIsFoo();
	}
	// end::customresolver[]

	@Test
	void fooResolver() {
		assertThat(ConfijBuilder.of(CustomResolver.class)
				.build()
				.everyVariableIsFoo()).isEqualTo("foo");
	}

	@Test
	void globalFooResolver() {
		assertThat(ConfijBuilder.of(BuilderNoop.class)
				.templatingWith(new FooResolver())
				.build()
				.canContainDollar()).isEqualTo("foo");
	}
}
