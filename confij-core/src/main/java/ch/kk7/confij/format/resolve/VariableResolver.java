package ch.kk7.confij.format.resolve;

import ch.kk7.confij.source.tree.ConfijNode;

@FunctionalInterface
public interface VariableResolver {
	String resolveValue(ConfijNode baseLeaf, String valueToResolve);

	default String resolveLeaf(ConfijNode leaf) {
		return resolveValue(leaf, leaf.getValue());
	}
}
