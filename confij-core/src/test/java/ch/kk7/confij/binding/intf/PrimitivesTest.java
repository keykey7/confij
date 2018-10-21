package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.binding.intf.PrimitivesTest.WithPrimitives;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class PrimitivesTest extends AbstractProxyBuilderTest<WithPrimitives> {
	interface WithPrimitives {
		static void ignored() {
			// noop
		}

		int anInt();

		boolean aBoolean();

		byte[] byteArray();
	}

	@Override
	protected Class<WithPrimitives> interfaceClass() {
		return WithPrimitives.class;
	}

	private PrimitivesTest whatever() {
		return withInt(0).withBoolean(false)
				.withByteArray(new byte[]{});
	}

	private PrimitivesTest withInt(Integer value) {
		set("anInt", value);
		return this;
	}

	private PrimitivesTest withBoolean(Boolean value) {
		set("aBoolean", value);
		return this;
	}

	private PrimitivesTest withByteArray(byte[] value) {
		set("byteArray", value);
		return this;
	}

	@Test
	public void allUninitialized() {
		assertThatThrownBy(this::instance).isInstanceOf(BindingException.class)
				.hasMessageContaining("anInt")
				.hasMessageContaining("byteArray");
	}

	@Test
	public void partiallyUninitialized() {
		// NOT withAnInt()
		withBoolean(false).withByteArray(new byte[]{});
		assertThatThrownBy(this::instance).isInstanceOf(BindingException.class)
				.hasMessageContaining("anInt")
				.satisfies(o -> assertThat(o.getMessage()).doesNotContain("byteArray"));
	}

	@Test
	public void zeroInt() {
		assertThat(whatever().instance()
				.anInt()).isEqualTo(0);
	}

	@Test
	public void nullInt() {
		assertThat(whatever().withInt(null)
				.instance()
				.anInt()).as("null is automapped to 0")
				.isEqualTo(0);
	}

	@Test
	public void randomInt() {
		int rand = ThreadLocalRandom.current()
				.nextInt(Integer.MAX_VALUE - 1) + 1;
		assertThat(whatever().withInt(rand)
				.instance()
				.anInt()).isEqualTo(rand);
	}

	@Test
	public void whateverEquals() {
		WithPrimitives first = whatever().instance();
		WithPrimitives second = instance();
		assertThat(first).isEqualTo(first)
				.isEqualTo(second)
				.isNotSameAs(second)
				.isNotEqualTo(new Object())
				.isNotEqualTo(whatever().instance());
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}

	@Test
	public void canCallStatic() {
		WithPrimitives.ignored();
	}
}
