package ch.kk7.confij.binding.map;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.ConfijDefinitionException;
import ch.kk7.confij.common.Util;
import com.fasterxml.classmate.ResolvedType;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ToString
public class MapBindingFactory implements ConfigBindingFactory<MapBinding> {
	private final Function<ResolvedType, ? extends MapBuilder> builderFactory;

	public MapBindingFactory() {
		this(MapBuilder::new);
	}

	public MapBindingFactory(Function<ResolvedType, ? extends MapBuilder> builderFactory) {
		this.builderFactory = builderFactory;
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
				throw new ConfijDefinitionException("Attempted to bind an invalid type {}, which extends Map<{},{}>. " +
						"However, we can only handle Map<String,?> as String is the only supported key type, sorry.", type, keyType,
						componentType);
			}
			if (Util.rawObjectType.equals(componentType)) {
				throw new ConfijDefinitionException("Attempted to bind an invalid type {}, which extends Map<{},{}>. " +
						"However, this value type is unbound which indicates it was a wildcard or an unbound generic type", type, keyType,
						componentType);
			}
			MapBuilder builder = builderFactory.apply(type);
			return Optional.of(new MapBinding(builder, bindingType.bindingFor(componentType), configBinder));
		}
		return Optional.empty();
	}
}
