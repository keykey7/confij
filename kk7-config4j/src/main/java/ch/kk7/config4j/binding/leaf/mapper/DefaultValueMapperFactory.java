package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.leaf.IValueMapper;
import ch.kk7.config4j.binding.leaf.ValueMapperFactory;
import com.fasterxml.classmate.ResolvedType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DefaultValueMapperFactory implements ValueMapperFactory {
	private final Map<Class<?>, IValueMapper<?>> customMappings;

	public DefaultValueMapperFactory() {
		customMappings = new HashMap<>();
		withMapping(String.class, s -> s);
		withMapping(boolean.class, DefaultValueMapperFactory::parseStrictBoolean);
		withMapping(byte.class, Byte::valueOf);
		withMapping(short.class, Short::parseShort);
		withMapping(int.class, Integer::parseInt);
		withMapping(long.class, Long::parseLong);
		withMapping(float.class, Float::parseFloat);
		withMapping(double.class, Double::parseDouble);
		withMapping(char.class, DefaultValueMapperFactory::parseStrictChar);
		withMapping(Path.class, s -> Paths.get(s));
		withMapping(Duration.class, new DurationMapper());
		// TODO: support Date formats
		// TODO: support TimeDiffs
		// TODO: support MemoryUnits
		// TODO: pass in annotation information to mapper to allow for customization
	}

	public static boolean parseStrictBoolean(String string) {
		if ("true".equals(string)) {
			return true;
		} else if ("false".equals(string)) {
			return false;
		}
		throw BooleanFormatException.forInputString(string);
	}

	public static char parseStrictChar(String string) {
		if (string == null || string.length() != 1) {
			throw CharFormatException.forInputString(string);
		}
		return string.charAt(0);
	}

	private static Optional<IValueMapper<?>> maybeEnum(Class<?> forClass) {
		if (forClass.isEnum()) {
			//noinspection unchecked
			return Optional.of(s -> Enum.valueOf((Class<? extends Enum>) forClass, s));
		}
		return Optional.empty();
	}

	private static Optional<Method> maybeMethod(String methodName, Class forClass) {
		return Arrays.stream(forClass.getMethods())
				.filter(method -> methodName.equals(method.getName()))
				.filter(method -> StaticFunctionMapper.isValidMethod(method, forClass))
				.findAny();
	}

	@SafeVarargs
	private static <T> Optional<T> firstOf(Optional<T>... optionals) {
		return Stream.of(optionals)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	public <T> DefaultValueMapperFactory withMapping(Class<T> forClass, IValueMapper<T> mapping) {
		customMappings.put(forClass, mapping);
		return this;
	}

	@Override
	public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		if (type.isAbstract() || type.isArray()) {
			return Optional.empty();
		}
		Class forClass = type.getErasedType();
		//noinspection unchecked
		Optional<IValueMapper<?>> response = firstOf(maybeSpecialMapper(forClass), maybeEnum(forClass), maybeFunctionMapper(forClass),
				maybeConstructorMapper(forClass));
		// cache it
		response.ifPresent(valueMapper -> customMappings.putIfAbsent(forClass, valueMapper));
		return response;
	}

	protected Optional<IValueMapper<?>> maybeSpecialMapper(Class forClass) {
		return Optional.ofNullable(customMappings.get(forClass));
	}

	protected <T> Optional<IValueMapper<T>> maybeConstructorMapper(Class<T> forClass) {
		//noinspection unchecked
		return Arrays.stream(forClass.getConstructors())
				.filter(constructor -> forClass.equals(constructor.getDeclaringClass()))
				.filter(constructor -> SoloConstructorMapper.isValidConstructor((Constructor<T>) constructor, forClass))
				.findAny()
				.map(constructor -> new SoloConstructorMapper(constructor, forClass));
	}

	protected <T> Optional<IValueMapper<T>> maybeFunctionMapper(Class<T> forClass) {
		return firstOf(maybeMethod("valueOf", forClass), maybeMethod("fromString", forClass)).map(
				method -> new StaticFunctionMapper<>(method, forClass));
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
