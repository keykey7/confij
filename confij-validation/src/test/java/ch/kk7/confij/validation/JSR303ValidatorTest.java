package ch.kk7.confij.validation;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.pipeline.Config4jBuilder;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
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
	public void testValid() {
		Config4jBuilder.of(ValidatedConfig.class)
				.build();
	}

	@Test
	public void testOneInvalid() {
		Config4jBuilder<ValidatedConfig> builder = Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource().with("anInt", "23"));
		assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(builder::build)
				.satisfies(e -> assertThat(e.getConstraintViolations()).hasSize(1));
	}

	@Test
	public void testNestedInvalid() {
		Config4jBuilder<ValidatedConfig> builder = Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource().with("nested.aString", ""));
		assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(builder::build);
	}

	@Test
	public void testNestedIgnored() {
		Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource().with("nestedIgnored.aString", ""))
				.build();
	}

	@Test
	public void testNestedSetInvalid() {
		Config4jBuilder<ValidatedConfig> builder = Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource()
						.with("aSet.0.aString", "")
						.with("aSet.1.aString", "I'm valid")
						.with("aSet.2.aString", ""));
		assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(builder::build);
		// TODO: actually not? .satisfies(e -> assertThat(e.getConstraintViolations()).hasSize(2));
	}

	@Test
	public void testNoValidator() {
		Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource().with("anInt", "23")
						.with("aSet.0.aString", ""))
				.withoutValidator()
				.build();
	}
}
