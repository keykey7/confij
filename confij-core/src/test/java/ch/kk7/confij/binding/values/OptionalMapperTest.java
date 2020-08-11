package ch.kk7.confij.binding.values;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.binding.ConfijBindingException;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class OptionalMapperTest implements WithAssertions {
	interface WithOptional {
		@Default("str")
		Optional<String> string();

		@Default("123456")
		Optional<Long> looong();

		Optional<String> empty();

		WithOptional INSTANCE = ConfijBuilder.of(WithOptional.class)
				.build();
	}

	interface Complex {
		Optional<WithOptional> complex();
	}

	@Test
	public void simple() {
		assertThat(WithOptional.INSTANCE.string()).hasValue("str");
		assertThat(WithOptional.INSTANCE.looong()).hasValue(123456L);
		assertThat(WithOptional.INSTANCE.empty()).isEmpty();
	}

	@Test
	public void complex() {
		assertThatThrownBy(() -> ConfijBuilder.of(Complex.class)
				.build()).isInstanceOf(ConfijBindingException.class)
				.hasMessageContaining(WithOptional.class.getSimpleName());
	}
}
