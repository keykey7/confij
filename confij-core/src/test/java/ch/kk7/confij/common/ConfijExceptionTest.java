package ch.kk7.confij.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfijExceptionTest {
	private IllegalStateException someCause = new IllegalStateException("some cause");

	@Test
	void noPlaceholders() {
		assertThat(new ConfijException("")).hasMessage("");
		assertThat(new ConfijException("some text")).hasMessage("some text")
				.hasNoCause();
		assertThat(new ConfijException("some text", someCause)).hasMessage("some text")
				.hasCause(someCause);
	}

	@Test
	void validPlaceholders() {
		assertThat(new ConfijException("{}", "placeholder")).hasMessage("placeholder");
		assertThat(new ConfijException("{}", "{}\\?>.:{ }")).hasMessage("{}\\?>.:{ }");
		assertThat(new ConfijException("{} {} {}", 1, 2, someCause)).as("placeholder wins")
				.hasMessage("1 2 " + someCause.toString());
	}

	@Test
	void invalidPlaceholders() {
		assertThat(new ConfijException("{}{}", "")).as("missing placeholder")
				.hasMessage("{?}");
		assertThat(new ConfijException("{},{},{}", 1, 2)).as("missing placeholder")
				.hasMessage("1,2,{?}");
		assertThat(new ConfijException("{}", 1, 2, 3)).as("unknown are appended")
				.hasMessage("1 2 3");
		assertThat(new ConfijException("{}", 1, 2, someCause)).as("cause is not appended")
				.hasMessage("1 2");
	}
}
