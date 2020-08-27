package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.BindingType;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface ValueMapperFactory {
	static <T> ValueMapperFactory always(@NonNull ValueMapperInstance<T> mapper) {
		return bindingType -> Optional.of(mapper);
	}

	static <T> ValueMapperFactory forClass(@NonNull ValueMapperInstance<T> mapper, @NonNull Class<T> forClass) {
		return bindingType -> {
			if (bindingType.getResolvedType()
					.getErasedType()
					.equals(forClass)) {
				return Optional.of(mapper);
			}
			return Optional.empty();
		};
	}

	static List<ValueMapperFactory> defaultFactories() {
		return Arrays.asList(ExplicitMapper.forString(), new PrimitiveMapperFactory(), new OptionalMapper(), ExplicitMapper.forFile(),
				ExplicitMapper.forPath(), new EnumMapper(), new DurationMapper(), new PeriodMapper(), new DateTimeMapper(),
				new StaticFunctionMapper(), new SoloConstructorMapper());
	}

	Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType);
}
