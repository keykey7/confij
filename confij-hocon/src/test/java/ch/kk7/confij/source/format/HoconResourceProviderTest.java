package ch.kk7.confij.source.format;

import ch.kk7.confij.ConfijBuilder;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

class HoconResourceProviderTest implements WithAssertions {

	interface Hocon {
		AandB aandb();

		AandB aandbIncluded();

		int[] intArr();

		double aFloat();

		boolean aBool();

		URL aNull();
	}

	interface AandB {
		int a();

		String b();
	}

	@Test
	public void complex() {
		Hocon hocon = ConfijBuilder.of(Hocon.class)
				.loadFrom("classpath:hocon.conf")
				.build();
		assertThat(hocon.aandb()).satisfies(x -> assertThat(x.a()).isEqualTo(42))
				.satisfies(x -> assertThat(x.b()).isEqualTo("43"));
		assertThat(hocon.aandbIncluded()).satisfies(x -> assertThat(x.a()).isEqualTo(1))
				.satisfies(x -> assertThat(x.b()).isEqualTo("2"));
		assertThat(hocon.intArr()).containsExactly(1, 2, 3, 4);
		assertThat(hocon.aFloat()).isEqualTo(3.333);
		assertThat(hocon.aBool()).isTrue();
		assertThat(hocon.aNull()).isNull();
	}

	@Test
	public void hoconResolvesThis() {
		assertThat(ConfijBuilder.of(AandB.class)
				.loadFrom("classpath:brefa.conf")
				.build()
				.b()).isEqualTo("1 and two");

		assertThat(ConfijBuilder.of(AandB.class)
				.loadFrom("classpath:brefa.conf")
				.templatingDisabled()
				.build()
				.b()).isEqualTo("1 and two");
	}

	@Test
	public void confijResolvesThis() {
		assertThat(ConfijBuilder.of(AandB.class)
				.loadFrom("classpath:bnotrefa.conf")
				.build()
				.b()).isEqualTo("1 and two");

		assertThat(ConfijBuilder.of(AandB.class)
				.loadFrom("classpath:bnotrefa.conf")
				.templatingDisabled()
				.build()
				.b()).isEqualTo("${a} and two");
	}
}
