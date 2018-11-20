package ch.kk7.confij.docs;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.pipeline.ConfijBuilder;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.time.Period;
import java.util.Map;
import java.util.Set;

public class Readme implements WithAssertions {

	interface House {
		@Default("true")
		boolean hasRoof();

		Map<String,Room> rooms();

		Set<@NotEmpty String> inhabitants();

		Period chimneyCheckEvery();

		@Default("${chimneyCheckEvery}")
		Period boilerCheckEvery();
	}

	interface Room {
		@Positive
		int numberOfWindows();

		@Default("Wood")
		FloorType floor();

		enum FloorType {
			Wood, Carpet, Stone
		}
	}

	@Test
	public void houseTest() {
		House johnsHouse = ConfijBuilder.of(House.class)
				.withSource("classpath:house.properties", "johnshouse.yaml")
				.build();
		System.out.println(johnsHouse);
		assertThat(johnsHouse.chimneyCheckEvery()).isEqualTo(Period.ofYears(2));
		assertThat(johnsHouse.boilerCheckEvery()).isEqualTo(Period.ofYears(2));
	}
}
