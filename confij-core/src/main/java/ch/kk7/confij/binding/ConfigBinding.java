package ch.kk7.confij.binding;

import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.ConfijNode;

/**
 * a structure derived from the configuration type (by parsing interfaces/...),
 * which can generate a {@link NodeDefinition} (usually done once),
 * and later bind a {@link ConfijNode} to an actual instance.
 * @param <T>
 */
public interface ConfigBinding<T> {
	NodeDefinition describe(NodeBindingContext nodeBindingContext);

	BindingResult<T> bind(ConfijNode config);
}
