package ch.kk7.confij.binding;

import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Collections;
import java.util.List;

/**
 * The outcome when buiding the final configuration object, but still keep it linked
 * to the old {@link ConfijNode} and its child-results. Facilitates post-processing,
 * such as with a {@link ch.kk7.confij.validation.ConfijValidator}.
 *
 * @param <T>
 */
@NonFinal
@Value
public class BindingResult<T> {
	T value;
	@NonNull ConfijNode node;
	@NonNull List<BindingResult<?>> children;

	public static <X> BindingResult<X> ofLeaf(X value, ConfijNode node) {
		return of(value, node, Collections.emptyList());
	}

	public static <X> BindingResult<X> of(X value, ConfijNode node, List<BindingResult<?>> children) {
		return new BindingResult<>(value, node, Collections.unmodifiableList(children));
	}
}
