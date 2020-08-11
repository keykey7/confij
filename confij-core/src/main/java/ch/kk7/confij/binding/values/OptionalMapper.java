package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.binding.leaf.LeafBindingFactory;
import com.fasterxml.classmate.ResolvedType;
import lombok.Value;

import java.util.Optional;

@SuppressWarnings("rawtypes")
public class OptionalMapper extends AbstractClassValueMapper<Optional> {
	public OptionalMapper() {
		super(Optional.class);
	}

	@Override
	public OptionalMapperInstance newInstance(BindingType bindingType) {
		ResolvedType innerResolvedType = bindingType.getResolvedType()
				.typeParametersFor(Optional.class)
				.get(0);
		BindingType innerBindingType = bindingType.bindingFor(innerResolvedType);
		Optional<ValueMapperInstance> innerMapper = LeafBindingFactory.firstValueMapper(innerBindingType);
		if (!innerMapper.isPresent()) {
			throw new ConfijBindingException(
					"cannot bind to type {}: {} only supports known leaf-types (direct values). This parameter isn't supported.",
					bindingType.getResolvedType(), this);
		} else {
			return new OptionalMapperInstance(innerMapper.get());
		}
	}

	@Value
	public static class OptionalMapperInstance<T> implements ValueMapperInstance<Optional<T>> {
		ValueMapperInstance<T> componentMapper;

		@Override
		public Optional<T> fromString(String string) {
			if (string == null) {
				return Optional.empty();
			}
			return Optional.of(componentMapper.fromString(string));
		}
	}
}
