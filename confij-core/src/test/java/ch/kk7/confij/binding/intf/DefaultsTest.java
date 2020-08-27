package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.intf.DefaultsTest.WithDefaults;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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

		default Path echoPath(Path input) {
			return input;
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

	private DefaultsTest withDouble(double value) {
		set("aRandomDouble", value);
		return this;
	}

	@Override
	protected Class<WithDefaults> interfaceClass() {
		return WithDefaults.class;
	}

	@Test
	void uninitializedString() {
		assertThat(instance().aString()).isEqualTo(DEFAULT_VALUE);
	}

	@Test
	void bindStringToValue() {
		String newValue = UUID.randomUUID() + "";
		WithDefaults withDefaults = withString(newValue).instance();
		assertThat(withDefaults.aString()).isEqualTo(newValue)
				.isSameAs(withDefaults.aString());
		assertThat(withDefaults.dependsOnAString()).isEqualTo(newValue + "X");
	}

	@Test
	void bindStringToNull() {
		WithDefaults withDefaults = withString(null).instance();
		assertThat(withDefaults.aString()).isNull();
		assertThat(withDefaults.dependsOnAString()).isEqualTo(null + "X");
	}

	@Test
	void uninitializedNullString() {
		assertThat(instance().aNull()).isNull();
	}

	@Test
	void aBoolean() {
		assertThat(instance().aBoolean()).isTrue();
		assertThat(withBoolean(true).instance()
				.aBoolean()).isTrue();
		assertThat(withBoolean(false).instance()
				.aBoolean()).isFalse();
		assertThat(withBoolean(null).instance()
				.aBoolean()).isFalse();
	}

	@Test
	void aRandom() {
		WithDefaults withDefaults = instance();
		assertThat(withDefaults.aRandomDouble()).isNotEqualTo(withDefaults.aRandomDouble());
	}

	@Test
	void aPath() {
		WithDefaults withDefaults = instance();
		Path path = Paths.get("fuu");
		assertThat(withDefaults.echoPath(path)).isEqualTo(path);
	}

	@Test
	void aRuntimeException() {
		assertThatThrownBy(() -> instance().aRuntimeException()).isInstanceOf(TestRuntimeException.class);
	}

	@Test
	void aCheckedException() {
		assertThatThrownBy(() -> instance().aCheckedException()).isInstanceOf(TestException.class);
	}

	@Test
	void canInitialize() {
		assertThat(instance()).isNotNull();
	}

	@Test
	void uninitializedEquals() {
		WithDefaults first = instance();
		WithDefaults second = instance();
		assertThat(first).isEqualTo(second)
				.isNotSameAs(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}

	@Test
	void proxyIsSerializable() {
		WithDefaults first = withString(UUID.randomUUID() + "").withDouble(1337)
				.instance();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(first);
		} catch (IOException e) {
			throw new RuntimeException("isn't serializable", e);
		}
		WithDefaults second;
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
			second = (WithDefaults) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("isn't deserializable", e);
		}
		assertThat(first).isEqualTo(second);
		assertThat(second.aRandomDouble()).isEqualTo(1337);
	}
}
