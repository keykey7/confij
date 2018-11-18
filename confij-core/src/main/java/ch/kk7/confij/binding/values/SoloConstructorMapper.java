package ch.kk7.confij.binding.values;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.values.ValueMapperInstance.NullableValueMapperInstance;
import ch.kk7.confij.common.Config4jException;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class SoloConstructorMapper implements ValueMapperFactory {
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(SoloConstructorMapper.class)
	public @interface SoloConstructor {
	}

	@RequiredArgsConstructor
	public class SoloConstructorMapperInstance<T> implements NullableValueMapperInstance<T> {
		@NonNull
		private final Constructor<T> constructor;

		@Override
		public T fromNonNullString(String string) {
			try {
				return constructor.newInstance(string);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new Config4jException("unable to instanitate object using constructor " + constructor, e);
			}
		}
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		ResolvedType type = bindingType.getResolvedType();
		return type.getConstructors()
				.stream()
				// .filter(constructor -> forClass.equals(constructor.getDeclaringClass()))
				.map(RawConstructor::getRawMember)
				.filter(constructor -> constructor.getParameterCount() == 1)
				.filter(constructor -> String.class.equals(constructor.getParameterTypes()[0]))
				.findAny()
				.map(SoloConstructorMapperInstance::new);
	}
}
