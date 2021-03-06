package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.ConfijDefinitionException;
import ch.kk7.confij.common.Util;
import com.fasterxml.classmate.ResolvedType;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ToString
public class CollectionBindingFactory implements ConfigBindingFactory<CollectionBinding> {
	private final Function<ResolvedType, ? extends CollectionBuilder> builderFactory;

	public CollectionBindingFactory() {
		this(CollectionBuilder::new);
	}

	public CollectionBindingFactory(Function<ResolvedType, ? extends CollectionBuilder> builderFactory) {
		this.builderFactory = builderFactory;
	}

	@NonNull
	public static ResolvedType collectionComponentType(ResolvedType type) {
		List<ResolvedType> typeParameters = type.typeParametersFor(Collection.class);
		if (typeParameters.size() != 1) {
			throw new IllegalStateException("Collection should always have 1 generic type, but found: " + typeParameters);
		}
		ResolvedType componentType = typeParameters.get(0);
		if (Util.rawObjectType.equals(componentType)) {
			throw new ConfijDefinitionException("Failed to determine the generic component type of Collection<?> for {}. " +
					"There is no upper bound for this generic parameter but we cannot bind to Object. " +
					type);
		}
		// otherwise: we have at least an upper bound for the generic (like <? extends Integer> becomes Integer.class)
		return componentType;
	}

	@Override
	public Optional<CollectionBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isInstanceOf(Collection.class)) {
			ResolvedType componentType = collectionComponentType(type);
			CollectionBuilder builder = builderFactory.apply(type);
			return Optional.of(new CollectionBinding(builder, bindingType.bindingFor(componentType), configBinder));
		}
		return Optional.empty();
	}
}
