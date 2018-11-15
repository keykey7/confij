package ch.kk7.confij.annotation;

import ch.kk7.confij.binding.leaf.ValueMapperFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ValueMapper {
	Class<? extends ValueMapperFactory> value();

	@Deprecated
	boolean force() default true;
}
