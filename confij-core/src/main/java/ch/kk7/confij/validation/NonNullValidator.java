package ch.kk7.confij.validation;

import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.Value;
import lombok.experimental.NonFinal;

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

@NonFinal
@Value
public class NonNullValidator implements ConfijValidator {
	Set<String> nullableNames = Stream.of(Nullable.class.getSimpleName(), "Null")
			.map(String::toLowerCase)
			.collect(Collectors.toSet());

	Set<String> notNullableNames = Stream.of(NotNull.class.getSimpleName(), "NonNull")
			.map(String::toLowerCase)
			.collect(Collectors.toSet());

	boolean rootIsNullable;

	public static NonNullValidator initiallyNullable() {
		return new NonNullValidator(true);
	}

	public static NonNullValidator initiallyNotNull() {
		return new NonNullValidator(false);
	}

	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface Nullable {
	}

	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface NotNull {
	}

	protected static boolean hasAnnotationAsIn(ConfijNode node, Set<String> listToCheck) {
		AnnotatedElement element = node.getConfig()
				.getNodeBindingContext()
				.getAnnotatedElement();
		if (element == null) { // is the case for example for GenericType<Whatever>
			return false;
		}
		return Arrays.stream(element.getDeclaredAnnotations())
				.map(x -> x.annotationType()
						.getSimpleName()
						.toLowerCase())
				.anyMatch(listToCheck::contains);
	}

	@Override
	public void validate(Object config) {
		throw new ConfijException("not to be called without ConfijNode");
	}

	@Override
	public void validate(Object config, ConfijNode rootNode) {
		validateNode(rootNode, rootIsNullable);
	}

	protected void validateNode(ConfijNode node, boolean defaultIsNullable) {
		final boolean isNullable;
		if (isNullable(node)) {
			if (isNonNullable(node)) {
				throw new ConfijException("conflicting annotations on {}: {}, as it matches both {} and {}", node, node.getConfig()
						.getNodeBindingContext()
						.getAnnotatedElement(), getNullableNames(), getNotNullableNames());
			}
			isNullable = true;
		} else if (isNonNullable(node)) {
			isNullable = false;
		} else {
			isNullable = defaultIsNullable;
		}

		if (!isNullable &&
				node.getConfig()
						.isValueHolder() &&
				node.getValue() == null) {
			throw new ConfijValidationException("unexpected null-value at {}", node.getUri());
		} else {
			for (ConfijNode child : node.getChildren()
					.values()) {
				validateNode(child, isNullable);
			}
		}
	}

	protected boolean isNonNullable(ConfijNode node) {
		return hasAnnotationAsIn(node, getNotNullableNames());
	}

	protected boolean isNullable(ConfijNode node) {
		return hasAnnotationAsIn(node, getNullableNames());
	}
}
