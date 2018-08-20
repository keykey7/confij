package ch.kk7.config4j.binding.collection;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.common.Util;
import ch.kk7.config4j.binding.ConfigBindingFactory;
import ch.kk7.config4j.binding.ConfigBinder;
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
				.map(Class::toGenericString)
				.collect(Collectors.joining(","));
	}

	@Override
	public Optional<CollectionBinding> maybeCreate(ResolvedType type, ConfigBinder configBinder) {
		if (type.isInstanceOf(Collection.class)) {
			List<ResolvedType> typeParameters = type.typeParametersFor(Collection.class);
			if (typeParameters.size() != 1) {
				throw new IllegalStateException("Collection with file params " + typeParameters);
			}
			ResolvedType componentType = typeParameters.get(0);
			if (Util.rawObjectType.equals(componentType)) {
				throw new Config4jException("cannot resolveString raw argument of Collection<?> for " + type);
			}
			UnmodifiableCollectionBuilder<?, ?> builder = builders.stream()
					.filter(b -> type.getErasedType()
							.isAssignableFrom(b.getInstanceClass()))
					.findFirst()
					.orElseThrow(() -> new Config4jException(
							"cannot handle collection of " + type + ", supported are " + supportedCollectionClasses()));
			//noinspection unchecked
			return Optional.of(new CollectionBinding(builder, componentType, configBinder));
		}
		return Optional.empty();
	}
}
