package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.ToString;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Spawns a new thread to reload configuration periodically. Never blocks.
 * @param <T>
 */
@ToString
public class ScheduledReloader<T> implements ConfijReloader<T> {
	private final Duration reloadEvery;
	private final Duration initialDelay;
	private final ScheduledExecutorService executor;
	private boolean isInitialized = false;
	private T current;

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
	public void initialize(ConfijPipeline<T> pipeline) {
		synchronized (this) {
			if (isInitialized) {
				throw new IllegalStateException("already initialized");
			}
			isInitialized = true;
		}
		current = pipeline.build();
		executor.scheduleWithFixedDelay(() -> {
			Thread.currentThread()
					.setName("ConfijReload");
			try {
				current = pipeline.build();
			} catch (Exception e) {
				// FIXME: requires log module
				throw e;
			}
		}, initialDelay.toMillis(), reloadEvery.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public T get() {
		return current;
	}
}
