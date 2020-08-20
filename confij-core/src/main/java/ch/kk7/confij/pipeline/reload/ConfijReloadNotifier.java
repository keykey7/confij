package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionList;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@NonFinal
@NoArgsConstructor
public class ConfijReloadNotifier<T> {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(ConfijReloadNotifier.class);

	@NonFinal
	BindingResult<T> lastBindingResult = null;

	Map<URI, List<ReloadHandler<?>>> registeredHandlers = new LinkedHashMap<>();

	Object $lockBindingResult = new Object();

	@FunctionalInterface
	public interface ReloadHandler<X> {
		void onReload(ReloadEvent<X> reloadEvent);
	}

	@Value
	public static class ReloadEvent<X> {
		X oldValue;

		X newValue;

		@NonNull URI eventPath;

		@NonNull Set<URI> changedPaths;

		public static <X> ReloadEvent<X> newOf(BindingResult<X> oldBindingResult, BindingResult<X> newBindingResult) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(oldBindingResult.getValue(), newBindingResult.getValue(), selfUri, Collections.singleton(selfUri));
		}

		public static <X> ReloadEvent<X> merge(BindingResult<X> oldBindingResult, BindingResult<X> newBindingResult,
				Set<ReloadEvent<?>> childChanges) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(oldBindingResult.getValue(), newBindingResult.getValue(), selfUri, childChanges.stream()
					.flatMap(x -> x.getChangedPaths()
							.stream())
					.collect(Collectors.toSet()));
		}

		public static <X> ReloadEvent<X> removed(BindingResult<X> oldBindingResult) {
			URI selfUri = oldBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(oldBindingResult.getValue(), null, selfUri, Collections.singleton(selfUri));
		}

		public static <X> ReloadEvent<X> added(BindingResult<X> newBindingResult) {
			URI selfUri = newBindingResult.getNode()
					.getUri();
			return new ReloadEvent<>(null, newBindingResult.getValue(), selfUri, Collections.singleton(selfUri));
		}
	}

	protected static Map<URI, BindingResult<?>> toChildMappings(BindingResult<?> bindingResult) {
		return bindingResult.getChildren()
				.stream()
				.collect(Collectors.toMap(x -> x.getNode()
						.getUri(), x -> x, (x1, x2) -> x1, LinkedHashMap::new));
	}

	@Synchronized("$lockBindingResult")
	public Optional<ReloadEvent<T>> configReloaded(@NonNull BindingResult<T> newBindingResult) {
		final Optional<ReloadEvent<T>> result;
		if(lastBindingResult == null) { // this is the first config ever
			result = Optional.empty();
		} else {
			result = notifyAllChangedValues(lastBindingResult, newBindingResult);
		}
		lastBindingResult = newBindingResult;
		return result;
	}

	@SuppressWarnings("unchecked")
	protected void maybeNotify(ReloadEvent reloadEvent) {
		Optional.ofNullable(registeredHandlers.get(reloadEvent.getEventPath()))
				.ifPresent(x -> x.forEach(handler -> handler.onReload(reloadEvent)));
	}

	@SuppressWarnings("unchecked")
	protected <X> Optional<ReloadEvent<X>> notifyAllChangedValues(BindingResult<X> oldBindingResult, BindingResult<X> newBindingResult) {
		if (oldBindingResult.getNode()
				.getConfig()
				.isValueHolder()) {
			boolean isSameConfigValue = Objects.equals(oldBindingResult.getNode()
					.getValue(), newBindingResult.getNode()
					.getValue());
			if (isSameConfigValue) {
				return Optional.empty();
			}
			return Optional.of(ReloadEvent.newOf(oldBindingResult, newBindingResult));
		}
		// looping over children and finding matching BindingResults (in new AND old)
		Map<URI, BindingResult<?>> oldChildMappings = toChildMappings(oldBindingResult);
		Map<URI, BindingResult<?>> newChildMappings = toChildMappings(newBindingResult);

		Set<ReloadEvent<?>> childReloadEvents = new LinkedHashSet<>();
		oldChildMappings.forEach((oldUri, oldChildBindingResult) -> {
			BindingResult newChildBindingResult = newChildMappings.get(oldUri);
			if (newChildBindingResult == null) { // was dropped
				childReloadEvents.add(ReloadEvent.removed(oldChildBindingResult));
			} else { // same node still exists
				notifyAllChangedValues(oldChildBindingResult, newChildBindingResult).ifPresent(
						x -> childReloadEvents.add((ReloadEvent<?>) x));
				newChildMappings.remove(oldUri); // update on the go
			}
		});
		// leftovers: the ones freshly added
		newChildMappings.forEach((uri, newChildBindingResult) -> childReloadEvents.add(ReloadEvent.added(newChildBindingResult)));

		if (childReloadEvents.isEmpty()) {
			return Optional.empty();
		}
		childReloadEvents.forEach(this::maybeNotify);
		ReloadEvent combinedReloadEvent = ReloadEvent.merge(oldBindingResult, newBindingResult, childReloadEvents);
		maybeNotify(combinedReloadEvent);
		return Optional.of(combinedReloadEvent);
	}

	@Synchronized("$lockBindingResult")
	public void registerRootReloadHandler(@NonNull ReloadHandler<T> reloadHandler) {
		registerReloadHandlerInternal(lastBindingResult.getNode()
				.getUri(), reloadHandler);
	}

	@Synchronized("$lockBindingResult")
	public <X> void registerReloadHandler(@NonNull X onConfigObject, @NonNull ReloadHandler<X> reloadHandler) {
		Set<BindingResult<X>> results = new LinkedHashSet<>();
		findSameValue(lastBindingResult, onConfigObject, results);
		if (results.isEmpty()) {
			throw new ConfijException(
					"unknown configuration Object {}. cannot register a reload handler on this. Is this an object you got using {}?",
					onConfigObject, ConfijBuilder.class + ".build()");
		}
		if (results.size() > 1) {
			throw new ConfijException(
					"non unique configuration Object {}. cannot register a reload handler on this. the following config paths all have the same value: {}",
					results.stream()
							.map(x -> x.getNode()
									.getUri())
							.collect(Collectors.toList()));
		}
		registerReloadHandlerInternal(results.iterator()
				.next()
				.getNode()
				.getUri(), reloadHandler);
	}

	protected <X> void registerReloadHandlerInternal(@NonNull URI nodeURI, @NonNull ReloadHandler<X> reloadHandler) {
		List<ReloadHandler<?>> registered = registeredHandlers.computeIfAbsent(nodeURI, x -> new ArrayList<>());
		if (registered.contains(reloadHandler)) {
			throw new ConfijException("this {} is already registered on path '{}': {}", ReloadHandler.class.getSimpleName(), nodeURI,
					reloadHandler);
		}
		registered.add(reloadHandler);
	}

	protected <X> void findSameValue(BindingResult<?> current, @NonNull X onConfigObject, Set<BindingResult<X>> results) {
		for (BindingResult<?> child : current.getChildren()) {
			findSameValue(child, onConfigObject, results);
		}
		if (current.getValue() == onConfigObject) { // NOT equals: we want exactly the same instance
			//noinspection unchecked
			results.add((BindingResult<X>) current);
		} else if (!results.isEmpty() &&
				current.getNode()
						.getConfig() instanceof NodeDefinitionList) {
			LOGGER.info("Ugh, registering a {} on a child of '{}', which is a list-like type is dangerous as is not well-defined " +
					"what will happen in cases were entries are added/removed", ReloadHandler.class.getSimpleName(), current.getNode()
					.getUri());
		}
	}
}
