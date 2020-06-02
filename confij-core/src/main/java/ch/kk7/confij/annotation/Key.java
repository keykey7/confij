package ch.kk7.confij.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides to which key in the configuration(-file) this value should be mapped.
 * The default key is always the method name:
 * <pre>
 * interface Fuu {
 *     \@Key("actualKey") String thisMethodNameIsIgnored();
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Key {
	String value();
}
