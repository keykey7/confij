package ch.kk7.confij.common;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.source.env.ExplicitPropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GenericTypeTest implements WithAssertions {
	@Test
	void asd() {
		List<Integer> listConfig = ConfijBuilder.of(new GenericType<List<Integer>>() {
		})
				.loadFrom(new ExplicitPropertiesSource().set("0", "42")
						.set("1", "1337"))
				.build();
		assertThat(listConfig).containsExactly(42, 1337);
	}
}
