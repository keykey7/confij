package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.annotation.ValueMapperFactory;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.IValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapperFactory;
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

public class Base64Mapper<T> implements IValueMapper<T> {
	private final Function<byte[], T> mapping;

	protected Base64Mapper(Function<byte[], T> mapping) {
		this.mapping = mapping;
	}

	@Override
	public T fromString(String string) {
		return mapping.apply(java.util.Base64.getDecoder()
				.decode(string));
	}

	@Inherited
	@Retention(RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapperFactory(Base64MapperFactory.class)
	public @interface Base64 {
	}

	// TODO: support different encodings and base64 standards
	// would require the BindingSettings to pass on the Base64 annotation itself
	public static class Base64MapperFactory implements IValueMapperFactory {
		@Override
		public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
			ResolvedType type = bindingType.getResolvedType();
			if (type.isArray()) {
				ResolvedType componentType = type.getArrayElementType();
				if (componentType.isInstanceOf(byte.class)) {
					return Optional.of(new Base64Mapper<>(x -> x));
				}
				// we could implement Byte.class here as well... but naaa
			}
			if (type.isInstanceOf(List.class)) {
				ResolvedType componentType = type.typeParametersFor(List.class)
						.get(0);
				if (componentType.isInstanceOf(Byte.class)) {
					return Optional.of(new Base64Mapper<>(arr -> {
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
}
