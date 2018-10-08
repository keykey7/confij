package ch.kk7.confij.pipeline;

@FunctionalInterface
public interface ConfijPipeline<T> {
	T build();
}
