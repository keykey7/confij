package ch.kk7.confij.binding.values;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Default;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

class ExplicitMapperTest implements WithAssertions {

	interface Explicit {
		@Default("str")
		String string();

		@Default("pat")
		Path path();

		@Default("fil")
		File file();

		Explicit INSTANCE = ConfijBuilder.of(Explicit.class)
				.build();
	}

	@Test
	void explicits() {
		assertThat(Explicit.INSTANCE.string()).isEqualTo("str");
		assertThat(Explicit.INSTANCE.path()).isEqualTo(Paths.get("pat"));
		assertThat(Explicit.INSTANCE.file()).isEqualTo(new File("fil"));
	}
}
