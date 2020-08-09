package ch.kk7.confij.validation;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.source.env.PropertiesSource;
import ch.kk7.confij.validation.NonNullValidator.Nullable;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

public class NonNullValidatorTest implements WithAssertions {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@interface Null {
	}

	interface WithNulls {
		String nothing();

		@Default("a default")
		String hasDefault();

		@Nullable
		String nullalbe();

		@Null
		String customNull();
	}

	private static final String FIELD_NAME = "nothing";

	@Test
	public void defaultBuilderAllowsNull() {
		assertThat(ConfijBuilder.of(WithNulls.class)
				.build()
				.nothing()).isNull();
	}

	@Test
	public void nullNotAllowed() {
		assertThatThrownBy(() -> ConfijBuilder.of(WithNulls.class)
				.validateNonNull()
				.build()).hasMessageContaining("null")
				.hasMessageContaining(FIELD_NAME);
	}

	@Test
	public void okIfNotNull() {
		String value = UUID.randomUUID() + "";
		assertThat(ConfijBuilder.of(WithNulls.class)
				.validateNonNull()
				.loadFrom(PropertiesSource.of(FIELD_NAME, value))
				.build()
				.nothing()).isEqualTo(value);
	}
}
