package ch.kk7.confij.template;

import ch.kk7.confij.tree.ConfijNode;

@FunctionalInterface
public interface ValueResolver {
	String resolveValue(ConfijNode baseLeaf, String valueToResolve);

	default String resolveLeaf(ConfijNode leaf) {
		return resolveValue(leaf, leaf.getValue());
	}
}
