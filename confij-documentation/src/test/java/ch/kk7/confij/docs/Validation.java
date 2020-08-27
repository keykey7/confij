package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import ch.kk7.confij.validation.ConfijValidationException;
import ch.kk7.confij.validation.NonNullValidator.Nullable;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Optional;

class Validation implements WithAssertions {

	// tag::jsr303-interface[]
	interface Jsr303Validated {
		@NotEmpty
		String mandatory();
		@Pattern(regexp = "[A-Z]*")
		String uppercase();
	}
	// end::jsr303-interface[]

	@Test
	void isJrs303Validated() {
		assertThatThrownBy(() -> ConfijBuilder.of(Jsr303Validated.class)
				.build()).isInstanceOf(ConfijValidationException.class)
				.hasCauseExactlyInstanceOf(ConstraintViolationException.class);
	}

	// tag::notnull-interface[]
	@NotNull
	interface NothingIsNull {
		String willCrashIfNull();
		@Nullable Long explicitlyNullable();
		Optional<Integer> allowedToBeEmpty();
	}
	// end::notnull-interface[]

	@Test
	void isNotNullValidated() {
		assertThatThrownBy(() -> ConfijBuilder.of(NothingIsNull.class)
        				.build()).isInstanceOf(ConfijValidationException.class);
		assertThat(ConfijBuilder.of(NothingIsNull.class)
								.loadFrom(PropertiesSource.of("willCrashIfNull", "xxx"))
                				.build().willCrashIfNull()).isEqualTo("xxx");
		assertThatThrownBy(() -> ConfijBuilder.of(NothingIsNull.class)
                				.build()).isInstanceOf(ConfijValidationException.class);
	}

	@Test
	void isNotNullDisabled() {
		assertThat(
			// tag::notnull-disabled-builder[]
			ConfijBuilder.of(NothingIsNull.class).validationAllowsNull().build()
			// end::notnull-disabled-builder[]
			.willCrashIfNull()
		).isNull();
	}
}
