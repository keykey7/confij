package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public interface ConfijReloadStrategy<T> {
	void register(ConfijPipeline<T> pipeline);
}
