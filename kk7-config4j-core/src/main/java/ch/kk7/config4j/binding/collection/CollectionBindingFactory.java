package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.binding.BindingException;
import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.common.Util;
import com.fasterxml.classmate.ResolvedType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CollectionBindingFactory implements ConfigBindingFactory<CollectionBinding> {
	private final Function<ResolvedType, ? extends CollectionBuilder> builderFactory;

	public CollectionBindingFactory() {
		this(CollectionBuilder::new);
	}

	public CollectionBindingFactory(Function<ResolvedType, ? extends CollectionBuilder> builderFactory) {
		this.builderFactory = builderFactory;
	}

	@Override
	public Optional<CollectionBinding> maybeCreate(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isInstanceOf(Collection.class)) {
			List<ResolvedType> typeParameters = type.typeParametersFor(Collection.class);
			if (typeParameters.size() != 1) {
				throw new IllegalStateException("Collection should always have 1 generic type, but found: " + typeParameters);
			}
			ResolvedType componentType = typeParameters.get(0);
			if (Util.rawObjectType.equals(componentType)) {
				throw new BindingException("cannot resolve the generic type within Collection<?> for " + type);
			}
			// otherwise: we have at least an upper bound for the generic (like <? extends Integer> becomes Integer.class)
			CollectionBuilder builder = builderFactory.apply(type);
			//noinspection unchecked
			return Optional.of(new CollectionBinding(builder, bindingType.bindingFor(componentType), configBinder));
		}
		return Optional.empty();
	}
}
