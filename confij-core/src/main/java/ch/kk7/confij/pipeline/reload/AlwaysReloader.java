package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public class AlwaysReloader<T> implements ConfijReloader<T> {
	private ConfijPipeline<T> pipeline;

	@Override
	public void initialize(ConfijPipeline<T> pipeline) {
		if (this.pipeline != null) {
			throw new IllegalStateException("already initialized");
		}
		this.pipeline = pipeline;
	}

	@Override
	public T get() {
		return pipeline.build();
	}
}
