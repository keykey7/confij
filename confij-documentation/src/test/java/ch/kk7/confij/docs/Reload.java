package ch.kk7.confij.docs;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.ConfijBuilder.ConfijWrapper;
import ch.kk7.confij.source.env.ExplicitPropertiesSource;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

class Reload extends DocTestBase {
	// tag::reload-interface[]
	interface ClientConfig {
		Endpoint database();
		Endpoint backend();
	}
	interface Endpoint {
		URL url();
		Duration timeout();
		boolean active();
	}
	// end::reload-interface[]

	@Test
	void reloadHandlers() {
		ExplicitPropertiesSource source = ExplicitPropertiesSource.of("database.url", "https://example.com/db")
				.set("backend.url", "https://example.com/be");

		// tag::reload-builder[]
		ConfijWrapper<ClientConfig> wrapper = ConfijBuilder.of(ClientConfig.class)
			.loadFrom(source)
			.reloadPeriodically(Duration.ofSeconds(2))
			.buildWrapper();
		ClientConfig currentConfig = wrapper.get(); // always the most up-to-date value
		// end::reload-builder[]
		assertThat(wrapper.get()
				.database()
				.active()).isFalse();

		// tag::reload-handler[]
		wrapper.getReloadNotifier().registerReloadHandler(
				reloadEvent -> resetHttpClient(reloadEvent.getNewValue()), // <1>
				currentConfig.backend()); // <2>

		wrapper.getReloadNotifier().<Boolean>registerReloadHandler(
				reloadEvent -> setDbActivityTo(reloadEvent.getNewValue()),
                currentConfig.database(), "active"); // <3>
		// end::reload-handler[]

		assertThat(dbIsActive).isFalse();
		source.set("database.active", "true");
		await().atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> assertThat(dbIsActive).isTrue());
		await().atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> assertThat(wrapper.get()
						.database()
						.active()).as("wrapper " + wrapper + " not matching dbIsActive=" + dbIsActive)
						.isTrue());
	}

	private static void resetHttpClient(Endpoint endpoint) {
		// noop
	}

	AtomicBoolean dbIsActive = new AtomicBoolean(false);

	private void setDbActivityTo(boolean isActive) {
		dbIsActive.set(isActive);
	}
}
