package ch.kk7.confij.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public interface ConfijReloader<T> {
	void initialize(ConfijPipeline<T> pipeline);
	T get();
}
