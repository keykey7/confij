package ch.kk7.confij.binding.leaf;

import ch.kk7.confij.binding.BindingType;
import lombok.NonNull;

import java.util.Optional;

@FunctionalInterface
public interface ValueMapperFactory {
	Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType);

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
}
