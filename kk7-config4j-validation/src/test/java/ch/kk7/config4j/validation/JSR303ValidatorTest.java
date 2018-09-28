package ch.kk7.config4j.validation;

import ch.kk7.config4j.annotation.Default;
import ch.kk7.config4j.pipeline.Config4jBuilder;
import ch.kk7.config4j.source.env.PropertiesSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JSR303ValidatorTest {

	public interface ValidatedConfig {
		@Min(100)
		@Max(1000)
		@Default("123")
		int anInt();

		@Pattern(regexp = "A+")
		@Default("AAAAA")
		String aString();

		Set<@Valid NestedValidatedConfig> aSet();
	}

	public interface NestedValidatedConfig {
		@NotEmpty
		@Default("AAAAA")
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

	@Disabled("nested objects are not working yet... still proxy obj issues")
	@Test
	public void testNestedInvalid() {
		Config4jBuilder<ValidatedConfig> builder = Config4jBuilder.of(ValidatedConfig.class)
				.withSource(new PropertiesSource()
						.with("aSet.0.aString", "")
						.with("aSet.1.aString", "I'm valid")
						.with("aSet.2.aString", ""));
		assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(builder::build)
				.satisfies(e -> assertThat(e.getConstraintViolations()).hasSize(2));
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
