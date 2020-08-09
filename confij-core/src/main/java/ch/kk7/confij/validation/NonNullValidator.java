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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class NonNullValidator implements ConfijValidator {
	Set<String> nullableNames = Stream.of(Nullable.class.getSimpleName(), "Null")
			.map(String::toLowerCase)
			.collect(Collectors.toSet());

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
			if (node.getValue() == null) {
				AnnotatedElement annotatedElement = node.getConfig()
						.getNodeBindingContext()
						.getAnnotatedElement();
				if (!isNullable(annotatedElement)) {
					throw new ConfijValidationException("unexpected null-value at {}", node);
				}
			}
		} else {
			for (ConfijNode child : node.getChildren()
					.values()) {
				validateNode(child);
			}
		}
	}

	protected boolean isNullable(AnnotatedElement element) {
		return Arrays.stream(element.getDeclaredAnnotations())
				.map(x -> x.annotationType()
						.getSimpleName()
						.toLowerCase())
				.anyMatch(getNullableNames()::contains);
	}
}
