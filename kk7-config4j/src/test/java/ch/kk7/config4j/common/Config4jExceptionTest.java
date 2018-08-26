package ch.kk7.config4j.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.hamcrest.junit.internal.ThrowableCauseMatcher.hasCause;
import static org.hamcrest.junit.internal.ThrowableMessageMatcher.hasMessage;
import static org.hamcrest.text.IsEmptyString.emptyString;

class Config4jExceptionTest {
	private IllegalStateException someCause = new IllegalStateException("some cause");

	@Test
	public void noPlaceholders() {
		assertThat(new Config4jException(""), hasMessage(emptyString()));
		assertThat(new Config4jException("some text"), allOf(hasMessage(is("some text")), hasCause(nullValue())));
		assertThat(new Config4jException("some text", someCause), allOf(hasMessage(is("some text")), hasCause(is(someCause))));
	}

	@Test
	public void validPlaceholders() {
		assertThat(new Config4jException("{}", "placeholder"), hasMessage(is("placeholder")));
		assertThat(new Config4jException("{}", "{}\\?>.:{ }"), hasMessage(is("{}\\?>.:{ }")));
		assertThat("placeholder wins", new Config4jException("{} {} {}", 1, 2, someCause), hasMessage(is("1 2 " + someCause.toString())));
	}

	@Test
	public void invalidPlaceholders() {
		assertThat("missing placeholder", new Config4jException("{}{}", ""), hasMessage(is("{?}")));
		assertThat("missing placeholder", new Config4jException("{},{},{}", 1, 2), hasMessage(is("1,2,{?}")));
		assertThat("unknown are appended", new Config4jException("{}", 1, 2, 3), hasMessage(is("1 2 3")));
		assertThat("cause is not appended", new Config4jException("{}", 1, 2, someCause), hasMessage(is("1 2")));
	}
}
