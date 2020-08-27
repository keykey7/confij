package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public interface ConfijReloadStrategy {
	void register(ConfijPipeline<?> pipeline);
}
