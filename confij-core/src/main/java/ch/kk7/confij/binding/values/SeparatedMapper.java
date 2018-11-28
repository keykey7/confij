package ch.kk7.confij.binding.values;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.collection.CollectionBindingFactory;
import ch.kk7.confij.binding.collection.CollectionBuilder;
import ch.kk7.confij.binding.leaf.LeafBindingFactory;
import com.fasterxml.classmate.ResolvedType;
import lombok.AllArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeparatedMapper implements ValueMapperFactory {
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(SeparatedMapper.class)
	public @interface Separated {
		/**
		 * @return a regular expression to split the input value into parts
		 * @see String#split(String)
		 */
		String separator() default ",";

		/**
		 * @return true in case every part should be trimmed before further processing
		 */
		boolean trim() default false;
	}

	@Separated
	private static final class AnnonHolder {
	}

	private static Separated getContext(BindingType bindingType) {
		return bindingType.getBindingContext()
				.getFactoryConfigFor(SeparatedMapper.class)
				.filter(Separated.class::isInstance)
				.map(Separated.class::cast)
				.orElse(AnnonHolder.class.getAnnotation(Separated.class));
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		final ResolvedType componentType;
		final Collector collector;
		if (type.isArray()) {
			componentType = type.getArrayElementType();
			collector = Collectors.collectingAndThen(Collectors.toList(), list -> {
				Object array = Array.newInstance(componentType.getErasedType(), list.size());
				for (int i = 0; i < list.size(); i++) {
					Array.set(array, i, list.get(i));
				}
				return array;
			});
		} else if (type.isInstanceOf(Collection.class)) {
			componentType = CollectionBindingFactory.collectionComponentType(type);
			collector = new CollectionBuilder(type).asCollector();
		} else {
			return Optional.empty();
		}
		Separated context = getContext(bindingType);
		//noinspection unchecked
		return LeafBindingFactory.firstValueMapper(bindingType.bindingFor(componentType))
				.map(componentMapper -> new SeparatedMapperInstance<>(componentMapper, collector, context));
	}

	@AllArgsConstructor
	public static class SeparatedMapperInstance<T, C> implements ValueMapperInstance<T> {
		private final ValueMapperInstance<C> componentMapper;
		private final Collector<C, ?, T> collector;
		private final Separated context;

		@Override
		public T fromString(String string) {
			String[] parts = string == null ? new String[]{} : string.split(context.separator());
			return Stream.of(parts)
					.map(s -> context.trim() ? s.trim() : s)
					.map(componentMapper::fromString)
					.collect(collector);
		}
	}
}
