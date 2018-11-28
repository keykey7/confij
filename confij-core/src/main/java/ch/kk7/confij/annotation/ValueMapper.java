package ch.kk7.confij.annotation;

import ch.kk7.confij.binding.values.ValueMapperFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ValueMapper {
	Class<? extends ValueMapperFactory> value();

	@Deprecated boolean force() default true;
}
