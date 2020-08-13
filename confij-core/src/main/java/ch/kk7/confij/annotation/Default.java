package ch.kk7.confij.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the default value for a configuration item (leaf).
 * Default values are loaded first and can be overridden by any other configuration source
 * (so they have lowest preference).
 * <pre>
 * interface Fuu {
 *     \@Default("42") int theAnswer();
 * }
 * </pre>
 *
 * @see ch.kk7.confij.source.defaults.DefaultSource
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {
	String value();

	/**
	 * @return true if the {@link #value()} is actually null
	 */
	boolean isNull() default false;
}
