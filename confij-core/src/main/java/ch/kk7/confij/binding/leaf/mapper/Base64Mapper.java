package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.ValueMapperFactory;
import ch.kk7.confij.binding.leaf.ValueMapperInstance;
import com.fasterxml.classmate.ResolvedType;
import lombok.AllArgsConstructor;
import lombok.NonNull;

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
		Base64Decoder decoder() default Base64Decoder.RFC4648;
	}

	public enum Base64Decoder {
		RFC4648,
		RFC4648_URLSAFE,
		RFC2045;

		public java.util.Base64.Decoder getDecoder() {
			switch (this) {
				case RFC4648:
					return java.util.Base64.getDecoder();
				case RFC4648_URLSAFE:
					return java.util.Base64.getUrlDecoder();
				case RFC2045:
					return java.util.Base64.getMimeDecoder();
				default:
					throw new IllegalStateException("unknown decoder for " + this);
			}
		}
	}

	@Base64
	private static final class AnnonHolder {
	}

	@AllArgsConstructor
	public static class Base64MapperInstance<T> implements ValueMapperInstance<T> {
		@NonNull
		private final java.util.Base64.Decoder decoder;
		@NonNull
		private final Function<byte[], T> mapping;

		@Override
		public T fromString(String string) {
			return mapping.apply(decoder.decode(string));
		}
	}

	public java.util.Base64.Decoder getDecoder(BindingType bindingType) {
		return bindingType.getBindingSettings()
				.getFactoryConfigFor(Base64Mapper.class)
				.filter(Base64.class::isInstance)
				.map(Base64.class::cast)
				.orElse(AnnonHolder.class.getAnnotation(Base64.class))
				.decoder()
				.getDecoder();
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isArray()) {
			ResolvedType componentType = type.getArrayElementType();
			if (componentType.isInstanceOf(byte.class)) {
				return Optional.of(new Base64MapperInstance<>(getDecoder(bindingType), x -> x));
			}
			// we could implement Byte.class here as well... but naaa
		}
		if (type.isInstanceOf(List.class)) {
			ResolvedType componentType = type.typeParametersFor(List.class)
					.get(0);
			if (componentType.isInstanceOf(Byte.class)) {
				return Optional.of(new Base64MapperInstance<>(getDecoder(bindingType), arr -> {
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
