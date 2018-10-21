package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.intf.DefaultsTest.WithDefaults;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class DefaultsTest extends AbstractProxyBuilderTest<WithDefaults> {
	private static final String DEFAULT_VALUE = "default";

	@SuppressWarnings("UnusedReturnValue")
	interface WithDefaults {
		default String aString() {
			return DEFAULT_VALUE;
		}

		default boolean aBoolean() {
			return true;
		}

		default double aRandomDouble() {
			return Math.random();
		}

		default String dependsOnAString() {
			return aString() + "X";
		}

		default String aNull() {
			return null;
		}

		default String aRuntimeException() {
			throw new TestRuntimeException();
		}

		default String aCheckedException() throws TestException {
			throw new TestException();
		}
	}

	private static class TestRuntimeException extends RuntimeException {
	}

	private static class TestException extends Exception {
	}

	private DefaultsTest withString(String value) {
		set("aString", value);
		return this;
	}

	private DefaultsTest withBoolean(Boolean value) {
		set("aBoolean", value);
		return this;
	}

	@Override
	protected Class<WithDefaults> interfaceClass() {
		return WithDefaults.class;
	}

	@Test
	public void uninitializedString() {
		assertThat(instance().aString()).isEqualTo(DEFAULT_VALUE);
	}

	@Test
	public void bindStringToValue() {
		String newValue = UUID.randomUUID() + "";
		WithDefaults withDefaults = withString(newValue).instance();
		assertThat(withDefaults.aString()).isEqualTo(newValue)
				.isSameAs(withDefaults.aString());
		assertThat(withDefaults.dependsOnAString()).isEqualTo(newValue + "X");
	}

	@Test
	public void bindStringToNull() {
		WithDefaults withDefaults = withString(null).instance();
		assertThat(withDefaults.aString()).isNull();
		assertThat(withDefaults.dependsOnAString()).isEqualTo(null + "X");
	}

	@Test
	public void uninitializedNullString() {
		assertThat(instance().aNull()).isNull();
	}

	@Test
	public void aBoolean() {
		assertThat(instance().aBoolean()).isTrue();
		assertThat(withBoolean(true).instance()
				.aBoolean()).isTrue();
		assertThat(withBoolean(false).instance()
				.aBoolean()).isFalse();
		assertThat(withBoolean(null).instance()
				.aBoolean()).isFalse();
	}

	@Test
	public void aRandom() {
		WithDefaults withDefaults = instance();
		assertThat(withDefaults.aRandomDouble()).isNotEqualTo(withDefaults.aRandomDouble());
	}

	@Test
	public void aRuntimeException() {
		assertThatThrownBy(() -> instance().aRuntimeException()).isInstanceOf(TestRuntimeException.class);
	}

	@Test
	public void aCheckedException() {
		assertThatThrownBy(() -> instance().aCheckedException()).isInstanceOf(TestException.class);
	}

	@Test
	public void canInitialize() {
		assertThat(instance()).isNotNull();
	}

	@Test
	public void uninitializedEquals() {
		WithDefaults first = instance();
		WithDefaults second = instance();
		assertThat(first).isEqualTo(second)
				.isNotSameAs(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
}
