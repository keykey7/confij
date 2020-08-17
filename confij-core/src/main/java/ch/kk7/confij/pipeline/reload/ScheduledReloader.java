package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spawns a new thread to reload configuration periodically. Never blocks.
 * @param <T>
 */
@ToString
public class ScheduledReloader<T> implements ConfijReloader<T> {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(ScheduledReloader.class);

	private final Duration reloadEvery;
	private final Duration initialDelay;
	private final ScheduledExecutorService executor;
	private boolean isInitialized = false;
	private final AtomicReference<T> current = new AtomicReference<>();

	public ScheduledReloader() {
		this(Duration.ofSeconds(30));
	}

	public ScheduledReloader(Duration reloadEvery) {
		this(reloadEvery, Duration.ofSeconds(60));
	}

	public ScheduledReloader(Duration reloadEvery, Duration initialDelay) {
		this.reloadEvery = reloadEvery;
		this.initialDelay = initialDelay;
		executor = Executors.newScheduledThreadPool(1);
	}

	@Override
	@Synchronized
	public void initialize(@NonNull ConfijPipeline<T> pipeline) {
		if (isInitialized) {
			throw new IllegalStateException("already initialized");
		}
		current.set(pipeline.build());
		isInitialized = true;
		executor.scheduleWithFixedDelay(() -> {
			Thread.currentThread()
					.setName("ConfijReload");
			try {
				current.set(pipeline.build());
			} catch (Exception e) {
				LOGGER.info("configuration reloading failed, will retry in {}ms", reloadEvery.toMillis(), e);
			}
		}, initialDelay.toMillis(), reloadEvery.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public T get() {
		if (!isInitialized) {
			throw new IllegalStateException("not initialized");
		}
		return current.get();
	}
}
