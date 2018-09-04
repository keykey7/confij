package ch.kk7.config4j.binding.map;

import ch.kk7.config4j.binding.BindingException;
import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBindingFactory;
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
				.collect(Collectors.joining(", "));
	}

	@Override
	public Optional<MapBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isInstanceOf(Map.class)) {
			List<ResolvedType> typeParameters = type.typeParametersFor(Map.class);
			if (typeParameters.size() != 2) {
				throw new IllegalStateException("Expected Map<K,V> with 2 generic params, but found " + typeParameters);
			}
			ResolvedType keyType = typeParameters.get(0);
			ResolvedType componentType = typeParameters.get(1);
			if (!keyType.isInstanceOf(String.class)) {
				// TODO: remove this limitation: any leaf type should be allowed as key
				throw new BindingException("Attempted to bind an invalid type {}, which extends Map<{},{}>. " +
						"However, we can only handle Map<String,?> as String is the only supported key type, sorry.", type, keyType,
						componentType);
			}
			if (rawObjectType.equals(componentType)) {
				throw new BindingException("Attempted to bind an invalid type {}, which extends Map<{},{}>. " +
						"However, this value type is unbound which indicates it was a wildcard or an unbound generic type", type, keyType,
						componentType);
			}
			UnmodifiableMapBuilder<?, ?> builder = builders.stream()
					.filter(b -> type.getErasedType()
							.isAssignableFrom(b.getInstanceClass()))
					.findFirst()
					.orElseThrow(() -> new BindingException("Your type '{}' is a Map, however this type is not supported. " +
							"Expected are types that are assignable from any of the implementing classes: {}. " +
							"If you need another Map implementation, consider adding an additional builder to {}", type,
							supportedMapClasses(), UnmodifiableMapBuilder.class.getName()));
			//noinspection unchecked
			return Optional.of(new MapBinding(builder, bindingType.bindingFor(componentType), configBinder));
		}
		return Optional.empty();
	}
}
