package ch.kk7.confij.source.logical;

import ch.kk7.confij.pipeline.ConfijBuilder;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.ConfijSourceTestBase;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaybeSourceTest extends ConfijSourceTestBase {

	private static AbstractStringAssert<?> assertThatX(ConfigSource nested) {
		ConfigX orConfig = ConfijBuilder.of(ConfigX.class)
				.withSource(setXTo("before"))
				.withSource(new MaybeSource(nested))
				.build();
		return assertThat(orConfig.x());
	}

	@Test
	public void exceptionAtStart() {
		assertThatX(alwaysFail).isEqualTo("before");
	}

	@Test
	public void exceptionAtEnd() {
		assertThatX(setThenFail).isEqualTo("before");
	}

	@Test
	public void noException() {
		assertThatX(setXTo("override")).isEqualTo("override");
	}

	@Test
	public void noNullSource() {
		assertThatThrownBy(() -> new MaybeSource(null));
	}
}