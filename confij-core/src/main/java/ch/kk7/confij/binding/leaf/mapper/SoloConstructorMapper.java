package ch.kk7.confij.binding.leaf.mapper;

import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.leaf.IValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapper.NullableValueMapper;
import ch.kk7.confij.binding.leaf.IValueMapperFactory;
import ch.kk7.confij.common.Config4jException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class SoloConstructorMapper<T> implements NullableValueMapper<T> {

	private final Constructor<T> constructor;

	protected SoloConstructorMapper(Constructor<T> constructor) {
		this.constructor = constructor;
	}

	@Override
	public T fromNonNullString(String string) {
		try {
			return constructor.newInstance(string);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new Config4jException("unable to instanitate obj using constructor " + constructor, e);
		}
	}

	public static class SoloConstructorMapperFactory implements IValueMapperFactory {
		@Override
		public Optional<IValueMapper<?>> maybeForType(BindingType bindingType) {
			ResolvedType type = bindingType.getResolvedType();
			return type.getConstructors()
					.stream()
					// .filter(constructor -> forClass.equals(constructor.getDeclaringClass()))
					.map(RawConstructor::getRawMember)
					.filter(constructor -> constructor.getParameterCount() == 1)
					.filter(constructor -> String.class.equals(constructor.getParameterTypes()[0]))
					.findAny()
					.map(SoloConstructorMapper::new);
		}
	}
}
