package ch.kk7.confij.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Config4jExceptionTest {
	private IllegalStateException someCause = new IllegalStateException("some cause");

	@Test
	public void noPlaceholders() {
		assertThat(new Config4jException("")).hasMessage("");
		assertThat(new Config4jException("some text")).hasMessage("some text")
				.hasNoCause();
		assertThat(new Config4jException("some text", someCause)).hasMessage("some text")
				.hasCause(someCause);
	}

	@Test
	public void validPlaceholders() {
		assertThat(new Config4jException("{}", "placeholder")).hasMessage("placeholder");
		assertThat(new Config4jException("{}", "{}\\?>.:{ }")).hasMessage("{}\\?>.:{ }");
		assertThat(new Config4jException("{} {} {}", 1, 2, someCause)).as("placeholder wins")
				.hasMessage("1 2 " + someCause.toString());
	}

	@Test
	public void invalidPlaceholders() {
		assertThat(new Config4jException("{}{}", "")).as("missing placeholder")
				.hasMessage("{?}");
		assertThat(new Config4jException("{},{},{}", 1, 2)).as("missing placeholder")
				.hasMessage("1,2,{?}");
		assertThat(new Config4jException("{}", 1, 2, 3)).as("unknown are appended")
				.hasMessage("1 2 3");
		assertThat(new Config4jException("{}", 1, 2, someCause)).as("cause is not appended")
				.hasMessage("1 2");
	}
}
