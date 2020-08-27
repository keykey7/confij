package ch.kk7.confij.source.logical;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceTestBase;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaybeSourceTest extends ConfijSourceTestBase {
	private static AbstractStringAssert<?> assertThatX(ConfijSource nested) {
		ConfigX orConfig = ConfijBuilder.of(ConfigX.class)
				.loadFrom(setXTo("before"))
				.loadFrom(new MaybeSource(nested))
				.build();
		return assertThat(orConfig.x());
	}

	@Test
	void exceptionAtStart() {
		assertThatX(alwaysFail).isEqualTo("before");
	}

	@Test
	void exceptionAtEnd() {
		assertThatX(setThenFail).isEqualTo("before");
	}

	@Test
	void noException() {
		assertThatX(setXTo("override")).isEqualTo("override");
	}

	@Test
	void noNullSource() {
		assertThatThrownBy(() -> new MaybeSource(null));
	}

	@Test
	void viaBuilder() {
		System.setProperty("app.x", "appx");
		ConfigX orConfig = ConfijBuilder.of(ConfigX.class)
				.loadFrom(setXTo("before"))
				.loadOptionalFrom("nonexistent.properties", "sys:app")
				.build();
		assertThat(orConfig.x()).isEqualTo("appx");
	}
}
