package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ConfijReloader which only reloads on request. The first thread to request an outdated configuration will block and
 * reload the configuration. Other threads continue using the old instance without delay while one thread is reloading.
 * @param <T>
 */
@ToString
public class TimedBlockingReloader<T> implements ConfijReloader<T> {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(TimedBlockingReloader.class);
	private final Duration pauseAfterSuccess;
	private final Duration pauseAfterFailure;
	private final Lock reloadLock = new ReentrantLock();
	private Instant nextReloadAt = Instant.MIN;
	private ConfijPipeline<T> pipeline;
	private final AtomicReference<T> current = new AtomicReference<>();

	public TimedBlockingReloader() {
		this(Duration.ofSeconds(30), Duration.ofSeconds(30));
	}

	public TimedBlockingReloader(Duration pauseAfterSuccess, Duration pauseAfterFailure) {
		this.pauseAfterSuccess = pauseAfterSuccess;
		this.pauseAfterFailure = pauseAfterFailure;
	}

	@Override
	@Synchronized
	public void initialize(@NonNull ConfijPipeline<T> pipeline) {
		if (this.pipeline != null) {
			throw new IllegalStateException("already initialized");
		}
		current.set(pipeline.build());
		success();
		this.pipeline = pipeline;
	}

	protected boolean shouldReload() {
		return nextReloadAt.isBefore(Instant.now());
	}

	protected void success() {
		nextReloadAt = Instant.now()
				.plus(pauseAfterSuccess);
	}

	protected void failure(Exception e) {
		nextReloadAt = Instant.now()
				.plus(pauseAfterFailure);
		LOGGER.info("configuration reloading failed, will retry at {}", nextReloadAt, e);
	}

	public T getUpdated() {
		reloadLock.lock();
		try {
			return doReload();
		} finally {
			reloadLock.unlock();
		}
	}

	@Override
	public T get() {
		if (pipeline == null) {
			throw new IllegalStateException("not initialized");
		}
		if (!shouldReload()) {
			return current.get();
		}
		if (reloadLock.tryLock()) {
			try {
				if (!shouldReload()) { // 2nd check within the lock
					return current.get();
				}
				return doReload();
			} finally {
				reloadLock.unlock();
			}
		}
		return current.get();
	}

	// expects atomic access
	protected T doReload() {
		try {
			current.set(pipeline.build());
			success();
		} catch (Exception e) {
			failure(e);
		}
		return current.get();
	}
}
