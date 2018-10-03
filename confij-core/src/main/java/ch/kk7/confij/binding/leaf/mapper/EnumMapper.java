package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.IValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapperFactory;
import com.fasterxml.classmate.ResolvedType;

import java.util.Optional;

public class EnumMapper<T extends Enum<T>> implements NullableValueMapper<Enum<T>> {
	private final Class<T> forClass;

	public EnumMapper(Class<T> forClass) {
		this.forClass = forClass;
	}

	@Override
	public Enum<T> fromNonNullString(String string) {
		return Enum.valueOf(forClass, string);
	}

	public static class EnumMapperFactory implements IValueMapperFactory {
		@Override
		public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
			ResolvedType type = bindingType.getResolvedType();
			return Optional.of(type.getErasedType())
					.filter(Class::isEnum)
					.map(forClass -> new EnumMapper(forClass));
		}
	}
}
