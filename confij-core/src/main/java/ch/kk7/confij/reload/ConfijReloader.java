package ch.kk7.confij.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConfijReloader<T> {
	@NonNull
	private final ConfijPipeline<T> pipeline;
	@NonNull
	private final ReloadStrategy reloadStrategy;

	private T current;
	private final Lock reloadLock = new ReentrantLock();

	@Builder
	public ConfijReloader(ConfijPipeline<T> pipeline, ReloadStrategy reloadStrategy) {
		this.pipeline = pipeline;
		this.reloadStrategy = reloadStrategy;
		current = doReload();
	}

	public T getUpdated() {
		reloadLock.lock();
		try {
			return doReload();
		} finally {
			reloadLock.unlock();
		}
	}

	public T get() {
		if (!reloadStrategy.shouldReload()) {
			return current;
		}
		if (reloadLock.tryLock()) {
			try {
				if (!reloadStrategy.shouldReload()) { // 2nd check within the lock
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
			reloadStrategy.success();
			return current;
		} catch (Exception e) {
			reloadStrategy.failure();
			throw e;
		}
	}
}
