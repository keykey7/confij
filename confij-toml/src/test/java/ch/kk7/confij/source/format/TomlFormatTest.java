package ch.kk7.confij.source.format;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.annotation.Key;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"squid:S1192", "squid:S00100"})
class TomlFormatTest {
	interface TomlSpec {
		String title();

		Owner owner();

		Database database();

		Map<String, Server> servers();

		interface Owner {
			String name();

			OffsetDateTime dob();
		}

		interface Database {
			String server();

			List<Integer> ports();

			@Key("connection_max")
			Long connectionMax();

			Boolean enabled();
		}

		interface Server {
			String ip();

			String dc();

			List<String> hosts();
		}
	}

	@Test
	void complexTomlFormatResolving() {
		TomlSpec spec = ConfijBuilder.of(TomlSpec.class)
				.loadFrom("classpath:toml-spec.toml")
				.build();

		TomlSpec.Owner owner = spec.owner();
		assertThat(owner.name()).isEqualTo("Tom Preston-Werner");
		assertThat(owner.dob()).isAtSameInstantAs(OffsetDateTime.parse("1979-05-27T07:32:00-08:00"));

		TomlSpec.Database database = spec.database();
		assertThat(database.server()).isEqualTo("192.168.1.1");
		assertThat(database.ports()).containsExactly(8001, 8001, 8002);
		assertThat(database.connectionMax()).isEqualTo(5000);
		assertThat(database.enabled()).isTrue();

		Map<String, TomlSpec.Server> servers = spec.servers();
		assertThat(servers).containsOnlyKeys("alpha", "beta");

		TomlSpec.Server alpha = servers.get("alpha");
		assertThat(alpha.ip()).isEqualTo("10.0.0.1");
		assertThat(alpha.dc()).isEqualTo("eqdc10");

		TomlSpec.Server beta = servers.get("beta");
		assertThat(beta.ip()).isEqualTo("10.0.0.2");
		assertThat(beta.dc()).isEqualTo("eqdc10");
		assertThat(beta.hosts()).containsExactly("alpha", "omega");
	}
}
