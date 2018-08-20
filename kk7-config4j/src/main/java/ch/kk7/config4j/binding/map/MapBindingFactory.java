package ch.kk7.config4j.binding.map;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.binding.ConfigBinder;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedObjectType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapBindingFactory implements ConfigBindingFactory<MapBinding> {
	private static final ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);
	private final List<UnmodifiableMapBuilder<?, ?>> builders;

	public MapBindingFactory() {
		this(Collections.singletonList(UnmodifiableMapBuilder.mapBuilder()));
	}

	public MapBindingFactory(List<UnmodifiableMapBuilder<?, ?>> builders) {
		this.builders = Collections.unmodifiableList(builders);
	}

	private String supportedMapClasses() {
		return builders.stream()
				.map(UnmodifiableMapBuilder::getInstanceClass)
				.map(Class::toGenericString)
				.collect(Collectors.joining(","));
	}

	@Override
	public Optional<MapBinding> maybeCreate(ResolvedType type, ConfigBinder configBinder) {
		if (type.isInstanceOf(Map.class)) {
			List<ResolvedType> typeParameters = type.typeParametersFor(Map.class);
			if (typeParameters.size() != 2) {
				throw new IllegalStateException("Map with file params " + typeParameters);
			}
			ResolvedType keyType = typeParameters.get(1);
			ResolvedType componentType = typeParameters.get(1);
			if (!keyType.isInstanceOf(String.class)) {
				throw new Config4jException("can only handle Map<String, ?> and not " + type);
			}
			if (rawObjectType.equals(componentType)) {
				throw new Config4jException("cannot resolveString raw argument of Map<String, ?> for " + type);
			}
			UnmodifiableMapBuilder<?, ?> builder = builders.stream()
					.filter(b -> type.getErasedType()
							.isAssignableFrom(b.getInstanceClass()))
					.findFirst()
					.orElseThrow(() -> new Config4jException("cannot handle map of " + type + ", supported are " + supportedMapClasses()));
			//noinspection unchecked
			return Optional.of(new MapBinding(builder, componentType, configBinder));
		}
		return Optional.empty();
	}
}
