package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Spawns a new thread to reload configuration periodically.
 */
@ToString
public class PeriodicReloadStrategy implements ConfijReloadStrategy {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(PeriodicReloadStrategy.class);
	private final Duration reloadEvery;
	private final Duration initialDelay;
	private final ScheduledExecutorService executor;
	private boolean isInitialized = false;

	public PeriodicReloadStrategy() {
		this(Duration.ofSeconds(30));
	}

	public PeriodicReloadStrategy(Duration reloadEvery) {
		this(reloadEvery, Duration.ofSeconds(60));
	}

	public PeriodicReloadStrategy(Duration reloadEvery, Duration initialDelay) {
		this.reloadEvery = assertPositive(reloadEvery);
		this.initialDelay = assertPositive(initialDelay);
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	private static Duration assertPositive(Duration duration) {
		if (duration.isNegative() || duration.isZero()) {
			throw new IllegalArgumentException("duration must be strictly positive: " + duration);
		}
		return duration;
	}

	@Override
	@Synchronized
	public void register(@NonNull ConfijPipeline<?> pipeline) {
		if (isInitialized) {
			throw new IllegalStateException("already initialized");
		}
		isInitialized = true;
		executor.scheduleWithFixedDelay(() -> {
			Thread.currentThread()
					.setName("ConfijReload");
			Instant start = Instant.now();
			LOGGER.debug("starting to reload ConfiJ configuration...");
			try {
				pipeline.build();
				Duration dt = Duration.between(start, Instant.now());
				LOGGER.debug("successfully reloaded configuration within {}ms", dt);
			} catch (Exception e) {
				LOGGER.info("configuration reloading failed, will retry in {}ms", reloadEvery.toMillis(), e);
			}
		}, initialDelay.toMillis(), reloadEvery.toMillis(), TimeUnit.MILLISECONDS);
	}
}
