package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;

public class NeverReloadStrategy<T> implements ConfijReloadStrategy<T> {
	@Override
	public void register(ConfijPipeline<T> pipeline) {
		// noop
	}
}
