package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.concurrent.atomic.AtomicReference;

public interface ConfijReloadNotifier<T> {
	@Value
	@NonFinal
	class AtomicReferenceReloadHandler<X> implements ConfijReloadHandler<X> {
		AtomicReference<X> reference;

		public AtomicReferenceReloadHandler(X obj) {
			reference = new AtomicReference<>(obj);
		}

		@Override
		public void onReload(ReloadEvent<X> reloadEvent) {
			reference.set(reloadEvent.getNewValue());
		}
	}

	void registerRootReloadHandler(@NonNull ConfijReloadHandler<T> reloadHandler);

	default <X> AtomicReference<X> registerAtomicReference(@NonNull X onConfigObject) {
		AtomicReferenceReloadHandler<X> handler = new AtomicReferenceReloadHandler<>(onConfigObject);
		registerReloadHandler(handler, onConfigObject);
		return handler.getReference();
	}

	<X> void registerReloadHandler(@NonNull ConfijReloadHandler<X> childReloadHandler, @NonNull Object parent, String childPath,
			String... childPaths);

	/**
	 * Listen for changes on a config object. Something you got using {@link ConfijBuilder#build()}.
	 *
	 * @param reloadHandler  handler to be called when a config object or one of it's children (nested objects) changes
	 * @param onConfigObject the config object on which to listen for changes. no primitives nor cached/reused types.
	 * @param <X>            the config type
	 */
	<X> void registerReloadHandler(@NonNull ConfijReloadHandler<X> reloadHandler, @NonNull X onConfigObject);
}
