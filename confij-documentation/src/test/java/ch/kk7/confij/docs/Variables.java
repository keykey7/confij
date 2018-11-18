package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.format.resolve.NoopResolver.NoopVariableResolver;
import ch.kk7.confij.pipeline.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class Variables implements WithAssertions {

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

	// tag::relative[]
	interface Letter {
		Salutation saluation();

		@Default("I dearly miss you, ${saluation.name}!") // <1>
		String body();

		Regards regards();
	}

	interface Regards {
		@Default("${.saluation.name}'s friend") // <2>
		String sender();

		@Default("Sincierly\n${sender}")
		String regards();
	}
	// end::relative[]

	@Test
	public void relative() {
		// tag::relative[]
		Letter letter = ConfijBuilder.of(Letter.class).build();
		assertThat(letter.regards().sender()).isEqualTo("John's friend");
		assertThat(letter.body()).isEqualTo("I dearly miss you, John!");
		// end::relative[]
	}

	@Test
	public void asd() {
		assertThat(".asd.x".split(Pattern.quote("."))).hasSize(3);
	}


	interface CustomResolvers {
		@Default("${a}${b}")
		String everyVariableIsFoo();

		@Default("${variable}")
		@NoopVariableResolver
		String variableLooksLike();
	}
}
