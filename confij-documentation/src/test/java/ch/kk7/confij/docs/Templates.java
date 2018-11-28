package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.annotation.VariableResolver;
import ch.kk7.confij.format.resolve.NoopResolver.NoopVariableResolver;
import ch.kk7.confij.pipeline.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import ch.kk7.confij.source.tree.ConfijNode;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class Templates implements WithAssertions {

	// tag::simple[]
	interface Salutation {
		@Default("John")
		String name();

		@Default("Hi ${name}")
		String hello();
	}
	// end::simple[]

	@Test
	public void simple() {
		Salutation salutation = ConfijBuilder.of(Salutation.class)
				.withSource(new PropertiesSource().with("name", "Bob"))
				.build();
		assertThat(salutation.hello()).isEqualTo("Hi Bob");
		salutation = ConfijBuilder.of(Salutation.class)
				.withSource(new PropertiesSource().with("hello", "Cya"))
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
	public void relative() {
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
	public void recursive() {
		Recursive recursive = ConfijBuilder.of(Recursive.class).build();
		assertThat(recursive.victim()).isEqualTo("Poor Alice!");
	}

	// tag::noop[]
	interface Noop {
		@NoopVariableResolver
		String canContainDollar();
	}
	// end::noop[]

	// tag::global-noop[]
	@NoopVariableResolver
	interface GlobalNoop {
		String canContainDollar();
	}
	// end::global-noop[]

	interface BuilderNoop {
		String canContainDollar();
	}

	@Test
	public void noop() {
		assertThat(ConfijBuilder.of(Noop.class)
				.withSource(new PropertiesSource().with("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");
		assertThat(ConfijBuilder.of(GlobalNoop.class)
				.withSource(new PropertiesSource().with("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");

		assertThat(
				// tag::builder-noop[]
		ConfijBuilder.of(BuilderNoop.class).withoutTemplating()
				// end::builder-noop[]
				.withSource(new PropertiesSource().with("canContainDollar", "${variable}"))
				.build().canContainDollar()).isEqualTo("${variable}");
	}

	// tag::customresolver[]
	static class FooResolver implements ch.kk7.confij.format.resolve.VariableResolver {
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
	public void fooResolver() {
		assertThat(ConfijBuilder.of(CustomResolver.class)
				.build()
				.everyVariableIsFoo()).isEqualTo("foo");
	}

	@Test
	public void globalFooResolver() {
		assertThat(ConfijBuilder.of(BuilderNoop.class)
				.withTemplating(new FooResolver())
				.build()
				.canContainDollar()).isEqualTo("foo");
	}
}
