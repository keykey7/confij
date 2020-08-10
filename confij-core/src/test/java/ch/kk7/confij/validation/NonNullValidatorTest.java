package ch.kk7.confij.validation;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.source.env.PropertiesSource;
import ch.kk7.confij.validation.NonNullValidator.NotNull;
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
		String aField();

		@Default("a default")
		String hasDefaultValue();

		@Nullable
		String nullablePreDefinedAnnotation();

		@Null
		String customNullAnnotationIsOk();
	}

	@NotNull
	interface WithNullsAnnotated {
		String aField();
	}

	interface WithNullsNested {
		WithNullsAnnotated nested();
	}

	private static final String FIELD_NAME = "aField";

	@Test
	public void defaultBuilderAllowsNull() {
		assertThat(ConfijBuilder.of(WithNulls.class)
				.build()
				.aField()).isNull();
	}

	@Test
	public void nullNotAllowedAsDefinedInBuilder() {
		assertThatThrownBy(() -> ConfijBuilder.of(WithNulls.class)
				.validateNonNull()
				.build()).hasMessageContaining("null")
				.hasMessageContaining(FIELD_NAME);
	}

	@Test
	public void okIfNotNullAsDefinedInBuilder() {
		String value = UUID.randomUUID() + "";
		assertThat(ConfijBuilder.of(WithNulls.class)
				.validateNonNull()
				.loadFrom(PropertiesSource.of(FIELD_NAME, value))
				.build()
				.aField()).isEqualTo(value);
	}

	@Test
	public void nullNotAllowedAsDefinedInCode() {
		assertThatThrownBy(() -> ConfijBuilder.of(WithNullsAnnotated.class)
				.build()).hasMessageContaining("null")
				.hasMessageContaining(FIELD_NAME);
	}

	@Test
	public void okIfNotNullAsDefinedInCode() {
		String value = UUID.randomUUID() + "";
		assertThat(ConfijBuilder.of(WithNullsAnnotated.class)
				.loadFrom(PropertiesSource.of(FIELD_NAME, value))
				.build()
				.aField()).isEqualTo(value);
	}

	@Test
	public void nestedNotNullalbe() {
		// a bit of an edge case: nested configs are never nullable. only the ones that hold a value are
		assertThatThrownBy(() -> ConfijBuilder.of(WithNullsNested.class)
				.build()).hasMessageContaining("null")
				.hasMessageContaining(FIELD_NAME);
	}
}
