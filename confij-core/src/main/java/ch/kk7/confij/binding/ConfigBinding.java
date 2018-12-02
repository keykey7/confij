package ch.kk7.confij.binding;

import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.ConfijNode;

public interface ConfigBinding<T> {
	NodeDefinition describe(NodeBindingContext nodeBindingContext);

	T bind(ConfijNode config);
}
