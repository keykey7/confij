package ch.kk7.confij.format.resolve;

import ch.kk7.confij.source.tree.ConfijNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NoopResolver implements VariableResolver {
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ch.kk7.confij.annotation.VariableResolver(NoopResolver.class)
	public @interface NoopVariableResolver {
	}

	@Override
	public String resolveValue(ConfijNode baseLeaf, String value) {
		return value;
	}
}
