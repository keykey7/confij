package ch.kk7.confij.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ConfijReloader which only reloads on request. The first thread to request an outdated configuration will block until and
 * reload the configuration. Other concurrent threads continue using the old instance without delay.
 * @param <T>
 */
@ToString
public class TimedBlockingReloader<T> implements ConfijReloader<T> {
	private final Duration pauseAfterSuccess;
	private final Duration pauseAfterFailure;
	private final Lock reloadLock = new ReentrantLock();
	private Instant nextReloadAt = Instant.MIN;
	private ConfijPipeline<T> pipeline;
	private T current;

	public TimedBlockingReloader() {
		this(Duration.ofSeconds(30), Duration.ofSeconds(30));
	}

	public TimedBlockingReloader(Duration pauseAfterSuccess, Duration pauseAfterFailure) {
		this.pauseAfterSuccess = pauseAfterSuccess;
		this.pauseAfterFailure = pauseAfterFailure;
	}

	@Override
	public void initialize(ConfijPipeline<T> pipeline) {
		synchronized (this) {
			if (this.pipeline != null) {
				throw new IllegalStateException("already initialized");
			}
			this.pipeline = Objects.requireNonNull(pipeline);
		}
		doReload();
	}

	protected boolean shouldReload() {
		return nextReloadAt.isBefore(Instant.now());
	}

	protected void success() {
		nextReloadAt = Instant.now()
				.plus(pauseAfterSuccess);
	}

	protected void failure() {
		nextReloadAt = Instant.now()
				.plus(pauseAfterFailure);
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
		if (!shouldReload()) {
			return current;
		}
		if (reloadLock.tryLock()) {
			try {
				if (!shouldReload()) { // 2nd check within the lock
					return current;
				}
				return doReload();
			} finally {
				reloadLock.unlock();
			}
		}
		return current;
	}

	// expects atomic access
	protected T doReload() {
		try {
			T current = pipeline.build();
			this.current = current;
			success();
			return current;
		} catch (Exception e) {
			failure();
			// FIXME: swallow exception here and use old instance
			throw e;
		}
	}
}
