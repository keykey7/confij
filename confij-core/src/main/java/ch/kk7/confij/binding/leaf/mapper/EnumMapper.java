package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.ValueMapperFactory;
import ch.kk7.confij.binding.leaf.ValueMapperInstance;
import ch.kk7.confij.binding.leaf.ValueMapperInstance.NullableValueMapperInstance;
import com.fasterxml.classmate.ResolvedType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

public class EnumMapper implements ValueMapperFactory {
	@RequiredArgsConstructor
	public class EnumMapperInstance<T extends Enum<T>> implements NullableValueMapperInstance<Enum<T>> {
		@NonNull
		private final Class<T> forClass;

		@Override
		public Enum<T> fromNonNullString(String string) {
			return Enum.valueOf(forClass, string);
		}
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		return Optional.of(type.getErasedType())
				.filter(Class::isEnum)
				.map(x -> (Class<? extends Enum>) x)
				.map(EnumMapperInstance::new);
	}
}
