package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.NonNull;

public class ManualReloadStrategy<T> implements ConfijReloadStrategy<T> {
	ConfijPipeline<T> pipeline;

	@Override
	public void register(@NonNull ConfijPipeline<T> pipeline) {
		if (this.pipeline != null) {
			throw new IllegalStateException("pipeline already registered");
		}
		this.pipeline = pipeline;
	}

	public void reload() {
		if (pipeline == null) {
			throw new IllegalStateException("pipeline not registered yet");
		}
		pipeline.build();
	}
}
