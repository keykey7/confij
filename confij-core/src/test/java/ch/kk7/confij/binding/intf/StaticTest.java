package ch.kk7.confij.binding.intf;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.binding.intf.StaticTest.WithStatics;
import org.junit.jupiter.api.Test;

public class StaticTest extends AbstractProxyBuilderTest<WithStatics> {
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
	public void newInstance() {
		assertThat(instance()).isNotNull();
	}

	@Test
	public void instanceMember() {
		assertThat(WithStatics.INSTANCE).isNotNull();
	}

	@Test
	public void staticMethod() {
		assertThat(WithStatics.aStatic()).isEqualTo("blupp");
	}
}
