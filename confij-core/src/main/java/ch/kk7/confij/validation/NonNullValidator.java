package ch.kk7.confij.validation;

import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeDefinition;
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
public class NonNullValidator<T> implements ConfijValidator<T> {
	Set<String> nullableNames = Stream.of(Nullable.class.getSimpleName(), "Null")
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
	Set<String> notNullableNames = Stream.of(NotNull.class.getSimpleName(), "NonNull")
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
	boolean rootIsNullable;

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

	/**
	 * implicit {@link Nullable} at configuration root
	 */
	public static <T> NonNullValidator<T> initiallyNullable() {
		return new NonNullValidator<>(true);
	}

	/**
	 * implicit {@link NotNull} at configuration root
	 */
	public static <T> NonNullValidator<T> initiallyNotNull() {
		return new NonNullValidator<>(false);
	}

	protected static boolean hasAnnotationAsIn(NodeDefinition nodeDefinition, Set<String> listToCheck) {
		AnnotatedElement element = nodeDefinition.getNodeBindingContext()
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
	public void validate(BindingResult<T> bindingResult) {
		validateNode(bindingResult, rootIsNullable);
	}

	protected boolean isNullableWithHistory(BindingResult<?> bindingResult, boolean defaultIsNullable) {
		NodeDefinition nodeDefinition = bindingResult.getNode()
				.getConfig();
		if (isNullable(nodeDefinition)) {
			if (isNonNullable(nodeDefinition)) {
				throw new ConfijException("conflicting annotations on {}: {}, as it matches both {} and {}", bindingResult.getNode(),
						nodeDefinition.getNodeBindingContext()
								.getAnnotatedElement(), getNullableNames(), getNotNullableNames());
			}
			return true;
		} else if (isNonNullable(nodeDefinition)) {
			return false;
		}
		return defaultIsNullable;
	}

	protected void validateNode(BindingResult<?> bindingResult, boolean defaultIsNullable) {
		boolean isNullable = isNullableWithHistory(bindingResult, defaultIsNullable);
		ConfijNode node = bindingResult.getNode();
		if (!isNullable &&
				node.getConfig()
						.isValueHolder() &&
				bindingResult.getValue() == null) {
			throw new ConfijValidationException("unexpected null-value at {}", node.getUri());
		} else {
			for (BindingResult<?> child : bindingResult.getChildren()) {
				validateNode(child, isNullable);
			}
		}
	}

	protected boolean isNonNullable(NodeDefinition nodeDefinition) {
		return hasAnnotationAsIn(nodeDefinition, getNotNullableNames());
	}

	protected boolean isNullable(NodeDefinition nodeDefinition) {
		return hasAnnotationAsIn(nodeDefinition, getNullableNames());
	}
}
