package ch.kk7.confij.validation;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Set;

class JSR303ValidatorTest implements WithAssertions {

	public interface ValidatedConfig {
		@Min(100)
		@Max(1000)
		@Default("123")
		int anInt();

		@Pattern(regexp = "NOTNUL+")
		@Default("NOTNULL")
		String aString();

		@NotNull
		@Valid
		NestedValidatedConfig nested();

		NestedValidatedConfig nestedIgnored();

		String nullValue();

		Set<@Valid NestedValidatedConfig> aSet();
	}

	public interface NestedValidatedConfig {
		@NotEmpty
		@Default("NOTNULL")
		String aString();
	}

	@Test
	void testValid() {
		ConfijBuilder.of(ValidatedConfig.class)
				.build();
	}

	@Test
	void testOneInvalid() {
		ConfijBuilder<ValidatedConfig> builder = ConfijBuilder.of(ValidatedConfig.class)
				.loadFrom(new PropertiesSource().set("anInt", "23"));
		assertThatExceptionOfType(ConfijValidationException.class).isThrownBy(builder::build)
				.withCauseExactlyInstanceOf(ConstraintViolationException.class)
				.satisfies(e -> assertThat(((ConstraintViolationException) e.getCause()).getConstraintViolations()).hasSize(1));
	}

	@Test
	void testNestedInvalid() {
		ConfijBuilder<ValidatedConfig> builder = ConfijBuilder.of(ValidatedConfig.class)
				.loadFrom(new PropertiesSource().set("nested.aString", ""));
		assertThatExceptionOfType(ConfijValidationException.class).isThrownBy(builder::build);
	}

	@Test
	void testNestedIgnored() {
		ConfijBuilder.of(ValidatedConfig.class)
				.loadFrom(new PropertiesSource().set("nestedIgnored.aString", ""))
				.build();
	}

	@Test
	void testNestedSetInvalid() {
		ConfijBuilder<ValidatedConfig> builder = ConfijBuilder.of(ValidatedConfig.class)
				.loadFrom(new PropertiesSource()
						.set("aSet.0.aString", "")
						.set("aSet.1.aString", "I'm valid")
						.set("aSet.2.aString", ""));
		assertThatExceptionOfType(ConfijValidationException.class).isThrownBy(builder::build)
				.withCauseExactlyInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void testNoValidator() {
		ConfijBuilder.of(ValidatedConfig.class)
				.loadFrom(new PropertiesSource().set("anInt", "23")
						.set("aSet.0.aString", ""))
				.validationDisabled()
				.build();
	}
}
