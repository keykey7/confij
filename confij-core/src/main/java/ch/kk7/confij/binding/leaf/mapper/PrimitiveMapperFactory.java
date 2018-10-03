package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.IValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapperFactory;
import com.fasterxml.classmate.ResolvedType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PrimitiveMapperFactory implements IValueMapperFactory {
	private final Map<Class<?>, IValueMapper<?>> mappings;

	public PrimitiveMapperFactory() {
		this.mappings = new HashMap<>();
		withMapping(boolean.class, PrimitiveMapperFactory::parseBoolean);
		withMapping(byte.class, Byte::parseByte);
		withMapping(short.class, Short::parseShort);
		withMapping(int.class, Integer::parseInt);
		withMapping(long.class, Long::parseLong);
		withMapping(float.class, Float::parseFloat);
		withMapping(double.class, Double::parseDouble);
		withMapping(char.class, PrimitiveMapperFactory::parseChar);
	}

	@Override
	public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		if (!type.isPrimitive()) {
			return Optional.empty();
		}
		return Optional.of(mappings.get(type.getErasedType()));

	}

	protected <T> void withMapping(Class<T> forClass, NullableValueMapper<T> mapping) {
		mappings.put(forClass, mapping);
	}

	public static boolean parseBoolean(String string) {
		if ("false".equals(string)) {
			return false;
		}
		if ("true".equals(string)) {
			return true;
		}
		throw BooleanFormatException.forInputString(string);
	}

	public static char parseChar(String string) {
		if (string.length() != 1) {
			throw CharFormatException.forInputString(string);
		}
		return string.charAt(0);
	}

	public static class BooleanFormatException extends IllegalArgumentException {
		public BooleanFormatException(String str) {
			super(str);
		}

		static BooleanFormatException forInputString(String str) {
			return new BooleanFormatException("For input string: \"" + str + "\"");
		}
	}

	public static class CharFormatException extends IllegalArgumentException {
		public CharFormatException(String str) {
			super(str);
		}

		static CharFormatException forInputString(String str) {
			return new CharFormatException("For input string: \"" + str + "\"");
		}
	}
}
