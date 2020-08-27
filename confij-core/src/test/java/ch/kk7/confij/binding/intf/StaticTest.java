package ch.kk7.confij.binding.intf;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.binding.intf.StaticTest.WithStatics;
import org.junit.jupiter.api.Test;

class StaticTest extends AbstractProxyBuilderTest<WithStatics> {
	interface WithStatics {
		static String aStatic() {
			return "blupp";
		}

		WithStatics INSTANCE = ConfijBuilder.of(WithStatics.class)
				.build();
	}

	@Override
	protected Class<WithStatics> interfaceClass() {
		return WithStatics.class;
	}

	@Test
	void newInstance() {
		assertThat(instance()).isNotNull();
	}

	@Test
	void instanceMember() {
		assertThat(WithStatics.INSTANCE).isNotNull();
	}

	@Test
	void staticMethod() {
		assertThat(WithStatics.aStatic()).isEqualTo("blupp");
	}
}
