package ch.kk7.confij.binding.values;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.binding.values.PrimitiveMapperFactory.BooleanFormatException;
import ch.kk7.confij.binding.values.PrimitiveMapperFactory.CharFormatException;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PrimitiveMapperFactoryTest implements WithAssertions {

	interface Primitive {
		@Default("true")
		boolean aBoolean();

		@Default("42")
		byte aByte();

		@Default("17")
		short aShort();

		@Default("-22")
		int anInt();

		@Default("9223372036854775807")
		long aLong();

		@Default("3.141")
		float aFloat();

		@Default("-3.141")
		double aDouble();

		@Default("X")
		char aChar();
	}

	@Test
	void defaults() {
		assertThat(ConfijBuilder.of(Primitive.class)
				.build()).satisfies(p -> {
			assertThat(p.aBoolean()).isTrue();
			assertThat(p.aByte()).isEqualTo((byte) 42);
			assertThat(p.aShort()).isEqualTo((short) 17);
			assertThat(p.anInt()).isEqualTo(-22);
			assertThat(p.aLong()).isEqualTo(Long.MAX_VALUE);
			assertThat(p.aFloat()).isCloseTo(3.141f, Offset.offset(0.001f));
			assertThat(p.aDouble()).isCloseTo(-3.141d, Offset.offset(0.001d));
			assertThat(p.aChar()).isEqualTo('X');
		});
	}

	@Test
	void bools() {
		assertThat(PrimitiveMapperFactory.parseBoolean("true")).isTrue();
		assertThat(PrimitiveMapperFactory.parseBoolean("false")).isFalse();
	}

	@ValueSource(strings = {"TRUE", "1", "", "\0", "False", "null"})
	@ParameterizedTest
	void notBools(String src) {
		assertThatThrownBy(() -> PrimitiveMapperFactory.parseBoolean(src)).isInstanceOf(BooleanFormatException.class)
				.hasMessageContaining("true")
				.hasMessageContaining("false");
	}

	@ValueSource(strings = {"a", "B", "1", "\0"})
	@ParameterizedTest
	void chars(String src) {
		assertThat(PrimitiveMapperFactory.parseChar(src)).isEqualTo(src.charAt(0));
	}

	@ValueSource(strings = {"abc", "", "☠️", " \0"})
	@ParameterizedTest
	void noChars(String src) {
		assertThatThrownBy(() -> PrimitiveMapperFactory.parseChar(src)).isInstanceOf(CharFormatException.class)
				.hasMessageContaining(src);
	}
}
