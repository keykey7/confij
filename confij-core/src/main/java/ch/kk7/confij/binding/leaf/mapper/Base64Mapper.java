package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.ValueMapperFactory;
import ch.kk7.confij.binding.leaf.ValueMapperInstance;
import com.fasterxml.classmate.ResolvedType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class Base64Mapper implements ValueMapperFactory {
	@Inherited
	@Retention(RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(Base64Mapper.class)
	public @interface Base64 {
	}

	public static class Base64MapperInstance<T> implements ValueMapperInstance<T> {
		private final Function<byte[], T> mapping;

		protected Base64MapperInstance(Function<byte[], T> mapping) {
			this.mapping = mapping;
		}

		@Override
		public T fromString(String string) {
			return mapping.apply(java.util.Base64.getDecoder()
					.decode(string));
		}
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isArray()) {
			ResolvedType componentType = type.getArrayElementType();
			if (componentType.isInstanceOf(byte.class)) {
				return Optional.of(new Base64MapperInstance<>(x -> x));
			}
			// we could implement Byte.class here as well... but naaa
		}
		if (type.isInstanceOf(List.class)) {
			ResolvedType componentType = type.typeParametersFor(List.class)
					.get(0);
			if (componentType.isInstanceOf(Byte.class)) {
				return Optional.of(new Base64MapperInstance<>(arr -> {
					List<Byte> result = new ArrayList<>(arr.length);
					for (byte b : arr) {
						result.add(b);
					}
					return result;
				}));
			}
		}
		return Optional.empty();
	}
}
