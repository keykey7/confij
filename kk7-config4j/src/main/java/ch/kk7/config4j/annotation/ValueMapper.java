package ch.kk7.config4j.annotation;

import ch.kk7.config4j.binding.leaf.IValueMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ValueMapper {
	Class<? extends IValueMapper> value();
}
