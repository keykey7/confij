package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.pipeline.reload.ConfijReloadHandler.ReloadEvent;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionList;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keep track of previous configuration values and compares them to new ones (when the source value / config string changes).
 * Will notify all listeners of the changed value. Given one nested value changes, the parent will also be notified of the change.
 * <p>
 * The class is internally thread-safe, but if a config is reloaded before all handlers are registered it will crash out.
 *
 * @param <T>
 */
@Value
@NonFinal
@NoArgsConstructor
public class ReloadNotifierImpl<T> implements ConfijReloadNotifier<T> {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(ReloadNotifierImpl.class);

	private static Set<Class<?>> NON_UNIQUE_TYPES = new HashSet<>(
			Arrays.asList(Boolean.class, Integer.class, Long.class, Double.class, Float.class, Character.class));

	Map<URI, List<ConfijReloadHandler<?>>> registeredHandlers = new LinkedHashMap<>();

	@NonFinal
	BindingResult<T> lastBindingResult = null;

	protected static Map<URI, BindingResult<?>> toChildMappings(BindingResult<?> bindingResult) {
		return bindingResult.getChildren()
				.stream()
				.collect(Collectors.toMap(x -> x.getNode()
						.getUri(), x -> x, (x1, x2) -> x1, LinkedHashMap::new));
	}

	@Synchronized
	public Optional<ReloadEvent<T>> configReloaded(@NonNull BindingResult<T> newBindingResult) {
		final Optional<ReloadEvent<T>> result;
		if (lastBindingResult == null) { // this is the first config ever
			result = Optional.empty();
		} else {
			Instant timestamp = Instant.now();
			result = notifyAllChangedValues(timestamp, lastBindingResult, newBindingResult);
			result.ifPresent(event -> LOGGER.info("configuration change (@{}) detected changes in {}", timestamp, event.getChangedPaths()));
		}
		lastBindingResult = newBindingResult;
		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void maybeNotify(ReloadEvent reloadEvent) {
		Optional.ofNullable(registeredHandlers.get(reloadEvent.getEventPath()))
				.ifPresent(x -> x.forEach(handler -> handler.onReload(reloadEvent)));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <X> Optional<ReloadEvent<X>> notifyAllChangedValues(Instant timestamp, BindingResult<X> oldBindingResult,
			BindingResult<X> newBindingResult) {
		if (oldBindingResult.getNode()
				.getConfig()
				.isValueHolder()) {
			boolean isSameConfigValue = Objects.equals(oldBindingResult.getNode()
					.getValue(), newBindingResult.getNode()
					.getValue());
			if (isSameConfigValue) {
				return Optional.empty();
			}
			return Optional.of(ConfijReloadHandler.ReloadEvent.newOf(timestamp, oldBindingResult, newBindingResult));
		}
		// looping over children and finding matching BindingResults (in new AND old)
		Map<URI, BindingResult<?>> oldChildMappings = toChildMappings(oldBindingResult);
		Map<URI, BindingResult<?>> newChildMappings = toChildMappings(newBindingResult);

		Set<ReloadEvent<?>> childReloadEvents = new LinkedHashSet<>();
		oldChildMappings.forEach((oldUri, oldChildBindingResult) -> {
			BindingResult newChildBindingResult = newChildMappings.get(oldUri);
			if (newChildBindingResult == null) { // was dropped
				childReloadEvents.add(ConfijReloadHandler.ReloadEvent.removed(timestamp, oldChildBindingResult));
			} else { // same node still exists
				notifyAllChangedValues(timestamp, oldChildBindingResult, newChildBindingResult).ifPresent(
						x -> childReloadEvents.add((ReloadEvent<?>) x));
				newChildMappings.remove(oldUri); // update on the go
			}
		});
		// leftovers: the ones freshly added
		newChildMappings.forEach((uri, newChildBindingResult) -> childReloadEvents.add(
				ConfijReloadHandler.ReloadEvent.added(timestamp, newChildBindingResult)));

		if (childReloadEvents.isEmpty()) {
			return Optional.empty();
		}
		childReloadEvents.forEach(this::maybeNotify);
		ReloadEvent combinedReloadEvent = ConfijReloadHandler.ReloadEvent.merge(timestamp, oldBindingResult, newBindingResult,
				childReloadEvents);
		maybeNotify(combinedReloadEvent);
		return Optional.of(combinedReloadEvent);
	}

	@Override
	@Synchronized
	public void registerRootReloadHandler(@NonNull ConfijReloadHandler<T> reloadHandler) {
		registerReloadHandlerOnUri(getLastBindingResult().getNode()
				.getUri(), reloadHandler);
	}

	@Override
	public <X> void registerReloadHandler(@NonNull ConfijReloadHandler<X> childReloadHandler, @NonNull Object parent, String childPath,
			String... childPaths) {
		registerReloadHandlerInternal(childReloadHandler, parent, Stream.concat(Stream.of(childPath), Stream.of(childPaths))
				.toArray(String[]::new));
	}

	/**
	 * Listen for changes on a config object. Something you got using {@link ConfijBuilder#build()}.
	 *
	 * @param reloadHandler  handler to be called when a config object or one of it's children (nested objects) changes
	 * @param onConfigObject the config object on which to listen for changes. no primitives nor cached/reused types.
	 * @param <X>            the config type
	 */
	@Override
	public <X> void registerReloadHandler(@NonNull ConfijReloadHandler<X> reloadHandler, @NonNull X onConfigObject) {
		registerReloadHandlerInternal(reloadHandler, onConfigObject);
	}

	@Synchronized
	protected void registerReloadHandlerInternal(ConfijReloadHandler<?> reloadHandler, Object onConfigObject, String... childPaths) {
		if (NON_UNIQUE_TYPES.contains(onConfigObject.getClass())) {
			throw new ConfijException("it is unsafe to register a ReloadHandler on {} (type {}). " +
					"all primitive types [int, boolean,...] as well as its boxing counterparts {} might not be unique " +
					"and therefore ConfiJ might not recognize the correct Object you meant.", onConfigObject.getClass()
					.getSimpleName(), NON_UNIQUE_TYPES.stream()
					.map(Class::getSimpleName)
					.collect(Collectors.toList()));
		}
		final URI uriOfObj = mustFindUniqueConfigObject(onConfigObject);
		URI targetUri = uriOfObj;
		for (String pathPart : childPaths) {
			targetUri = targetUri.resolve(pathPart);
		}
		try { // verify the childPath is valid
			getLastBindingResult().getNode()
					.resolve(targetUri);
		} catch (ConfijException e) { // TODO: use a less generic exception
			throw new ConfijException("failed to register a ReloadHandler on {}. " +
					"the parent instance {} ({}) was found, however not the child instance: {}", targetUri, uriOfObj, onConfigObject,
					e.getMessage(), e);
		}
		registerReloadHandlerOnUri(targetUri, reloadHandler);
	}

	@NonNull
	protected <X> URI mustFindUniqueConfigObject(X onConfigObject) {
		Set<BindingResult<X>> results = new LinkedHashSet<>();
		findSameValue(getLastBindingResult(), onConfigObject, results);
		if (results.isEmpty()) {
			throw new ConfijException("unknown configuration object: {} (type {}). " +
					"cannot register a ReloadHandler on this instance since the same object cannot be found. " +
					"Is must be an object you got using {}", onConfigObject, onConfigObject.getClass()
					.getName(), ConfijBuilder.class + "#build()");
		}
		if (results.size() > 1) {
			throw new ConfijException("non unique configuration object {}. cannot register a reload handler on this. " +
					"the following config paths are all the same instance: {}", onConfigObject, results.stream()
					.map(x -> x.getNode()
							.getUri())
					.collect(Collectors.toList()));
		}
		return results.iterator()
				.next()
				.getNode()
				.getUri();
	}

	protected <X> void registerReloadHandlerOnUri(@NonNull URI nodeURI, @NonNull ConfijReloadHandler<X> reloadHandler) {
		List<ConfijReloadHandler<?>> registered = registeredHandlers.computeIfAbsent(nodeURI, x -> new ArrayList<>());
		if (registered.contains(reloadHandler)) {
			throw new ConfijException("this {} is already registered on path '{}': {}", ConfijReloadHandler.class.getSimpleName(), nodeURI,
					reloadHandler);
		}
		registered.add(reloadHandler);
	}

	protected <X> void findSameValue(BindingResult<?> current, @NonNull X onConfigObject, Set<BindingResult<X>> results) {
		for (BindingResult<?> child : current.getChildren()) {
			findSameValue(child, onConfigObject, results);
		}
		if (current.getValue() == onConfigObject) {
			//noinspection unchecked
			results.add((BindingResult<X>) current);
		} else if (!results.isEmpty() &&
				current.getNode()
						.getConfig() instanceof NodeDefinitionList) {
			LOGGER.info("Ugh, registering a {} on a child of '{}', which is a list-like type is dangerous as is not well-defined " +
					"what will happen in cases were entries are added/removed", ConfijReloadHandler.class.getSimpleName(), current.getNode()
					.getUri());
		}
	}
}
