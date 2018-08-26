package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.binding.BindingException;
import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.common.Util;
import com.fasterxml.classmate.ResolvedType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectionBindingFactory implements ConfigBindingFactory<CollectionBinding> {
	private final List<UnmodifiableCollectionBuilder<?, ?>> builders;

	public CollectionBindingFactory() {
		this(Arrays.asList(UnmodifiableCollectionBuilder.setBuilder(), UnmodifiableCollectionBuilder.listBuilder()));
	}

	public CollectionBindingFactory(List<UnmodifiableCollectionBuilder<?, ?>> builders) {
		this.builders = Collections.unmodifiableList(builders);
	}

	private String supportedCollectionClasses() {
		return builders.stream()
				.map(UnmodifiableCollectionBuilder::getInstanceClass)
				.map(Class::getName)
				.collect(Collectors.joining(", "));
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
			UnmodifiableCollectionBuilder<?, ?> builder = builders.stream()
					.filter(b -> type.getErasedType()
							.isAssignableFrom(b.getInstanceClass()))
					.findFirst()
					// TODO: add an easy way to register (or extend) a collectionbuilder
					.orElseThrow(() -> new BindingException("Your type '{}' is a Collection, however this type is not supported. " +
							"Expected are types that are assignable from any of the implementing classes: {}. " +
							"If you need another Collection implementation, consider adding an additional builder to {}", type,
							supportedCollectionClasses(), CollectionBindingFactory.class.getName()));
			//noinspection unchecked
			return Optional.of(new CollectionBinding(builder, bindingType.bindingFor(componentType), configBinder));
		}
		return Optional.empty();
	}
}
