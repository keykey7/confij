package ch.kk7.confij.template;

import ch.kk7.confij.tree.ConfijNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NoopValueResolver implements ValueResolver {
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ch.kk7.confij.annotation.VariableResolver(NoopValueResolver.class)
	public @interface NoopResolver {
	}

	@Override
	public String resolveValue(ConfijNode baseLeaf, String value) {
		return value;
	}
}
