package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.ValueMapperFactory;
import ch.kk7.confij.binding.leaf.ValueMapperInstance;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractClassValueMapper<T> implements ValueMapperFactory {
	@NonNull
	private final Class<T> forClass;

	public abstract ValueMapperInstance<T> newInstance(BindingType bindingType);

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		if (bindingType.getResolvedType()
				.getErasedType()
				.equals(forClass)) {
			return Optional.of(newInstance(bindingType));
		}
		return Optional.empty();
	}
}
