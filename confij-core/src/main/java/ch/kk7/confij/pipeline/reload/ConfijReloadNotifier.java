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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Value
@NonFinal
@NoArgsConstructor
public class ConfijReloadNotifier<T> {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(ConfijReloadNotifier.class);

	@NonFinal
	BindingResult<T> lastBindingResult = null;

	Map<URI, List<ReloadHandler<?>>> registeredHandlers = new LinkedHashMap<>();

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

	// TODO: maybe worth an idea: if the config did NOT change, we could keep the old instance
	@Synchronized
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

	@SuppressWarnings({"unchecked", "rawtypes"})
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

	@Synchronized
	public void registerRootReloadHandler(@NonNull ReloadHandler<T> reloadHandler) {
		registerReloadHandlerOnUri(lastBindingResult.getNode()
				.getUri(), reloadHandler);
	}

	public <X> AtomicReference<X> registerAtomicReference(@NonNull X onConfigObject) {
		AtomicReferenceReloadHandler<X> handler = new AtomicReferenceReloadHandler<>(onConfigObject);
		registerReloadHandler(handler, onConfigObject);
		return handler.getReference();
	}

	@Value
	@NonFinal
	public static class AtomicReferenceReloadHandler<X> implements ReloadHandler<X> {
		AtomicReference<X> reference;

		public AtomicReferenceReloadHandler(X obj) {
			reference = new AtomicReference<>(obj);
		}

		@Override
		public void onReload(ReloadEvent<X> reloadEvent) {
			reference.set(reloadEvent.getNewValue());
		}
	}

	private static Set<Class<?>> NON_UNIQUE_TYPES = new HashSet<>(
			Arrays.asList(Boolean.class, Integer.class, Long.class, Double.class, Float.class, Character.class));

	@Synchronized
	public <X> void registerReloadHandler(@NonNull ReloadHandler<X> reloadHandler, @NonNull X onConfigObject, String ... childPaths) {
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
		findSameValue(lastBindingResult, onConfigObject, results);
		if (results.isEmpty()) {
			throw new ConfijException("unknown configuration object: {} (type {}). " +
					"cannot register a ReloadHandler on this instance since the same object cannot be found. " +
					"Is must be an object you got using {}", onConfigObject, onConfigObject.getClass()
					.getName(), ConfijBuilder.class + "#build()");
		}
		if (results.size() > 1) {
			throw new ConfijException(
					"non unique configuration object {}. cannot register a reload handler on this. " +
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

	protected <X> void registerReloadHandlerOnUri(@NonNull URI nodeURI, @NonNull ReloadHandler<X> reloadHandler) {
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
		if (current.getValue() == onConfigObject) {
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
