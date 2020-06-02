package ch.kk7.confij.validation;

import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Set;

@Value
public class NonNullValidator implements ConfijValidator{
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface Nullable {
	}

	@Override
	public void validate(Object config) {
		throw new ConfijException("not to be called without ConfijNode");
	}

	@Override
	public void validate(Object config, ConfijNode rootNode) {
		validateNode(rootNode);
	}

	protected void validateNode(ConfijNode node) {
		if (node.getConfig()
				.isValueHolder()) {
			node.getConfig().getNodeBindingContext().getAnnotatedElement()

			String value = node.getValue();
			if (value == null) {

			}
		}
	}

	protected final Set<String>

	protected void isNullable(AnnotatedElement element) {
		Arrays.stream(element.getDeclaredAnnotations()).filter(x -> x.annotationType().getSimpleName().toLowerCase())
	}
}
