package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public class NeverReloadStrategy implements ConfijReloadStrategy {
	@Override
	public void register(ConfijPipeline<?> pipeline) {
		// noop
	}
}
