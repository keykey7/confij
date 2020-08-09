package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.validation.ConfijValidationException;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Validation implements WithAssertions {

	// tag::jsr303-interface[]
	interface Validated {
		@NotNull
		String mandatory();
		@Pattern(regexp = "[A-Z]*")
		String uppercase();
	}
	// end::jsr303-interface[]

	@Test
	public void isValidated() {
		assertThatThrownBy(() -> ConfijBuilder.of(Validated.class)
				.build()).isInstanceOf(ConfijValidationException.class)
				.hasCauseExactlyInstanceOf(ConstraintViolationException.class);
	}
}
