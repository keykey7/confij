package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.NonNull;

public class ManualReloadStrategy implements ConfijReloadStrategy {
	ConfijPipeline<?> pipeline;

	@Override
	public void register(@NonNull ConfijPipeline<?> pipeline) {
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
