package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.binding.BindingResult;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * a handler to be called in case of changes on a specific configuration node.
 *
 * @param <X>
 */
@FunctionalInterface
public interface ConfijReloadHandler<X> {
	/**
	 * an event emitted in case a configuration change is detected
	 *
	 * @param <X> the type of the node where the change happened
	 */
	@Value
	class ReloadEvent<X> {
		/**
		 * timestamp after which the configuration was read and validated, but before anyone got notified.
		 */
		@NonNull Instant timestamp;
		/**
		 * the previous value
		 */
		X oldValue;
		/**
		 * the updated configuration value (could be equal to {@link #getOldValue()} for example when a nested value changed).
		 */
		X newValue;
		/**
		 * the URI representing the current node itself. heps detecting changes in conjunction with {@link #getChangedPaths()}.
		 */
		@NonNull URI eventPath;
		/**
		 * all the changed config values from all the nested objects combined. Always contains {@link #getEventPath()}, too.
		 */
		@NonNull Set<URI> changedPaths;

		public static <X> ReloadEvent<X> newOf(Instant timestamp, BindingResult<X> oldBindingResult, BindingResult<X> newBindingResult) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(timestamp, oldBindingResult.getValue(), newBindingResult.getValue(), selfUri,
					Collections.singleton(selfUri));
		}

		public static <X> ReloadEvent<X> merge(Instant timestamp, BindingResult<X> oldBindingResult, BindingResult<X> newBindingResult,
				Set<ReloadEvent<?>> childChanges) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(timestamp, oldBindingResult.getValue(), newBindingResult.getValue(), selfUri, childChanges.stream()
					.flatMap(x -> x.getChangedPaths()
							.stream())
					.collect(Collectors.toSet()));
		}

		public static <X> ReloadEvent<X> removed(Instant timestamp, BindingResult<X> oldBindingResult) {
			URI selfUri = oldBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(timestamp, oldBindingResult.getValue(), null, selfUri, Collections.singleton(selfUri));
		}

		public static <X> ReloadEvent<X> added(Instant timestamp, BindingResult<X> newBindingResult) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(timestamp, null, newBindingResult.getValue(), selfUri, Collections.singleton(selfUri));
		}
	}

	void onReload(ReloadEvent<X> reloadEvent);
}
