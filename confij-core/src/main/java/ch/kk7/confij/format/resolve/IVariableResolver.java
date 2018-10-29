package ch.kk7.confij.format.resolve;

import ch.kk7.confij.source.simple.ConfijNode;

public interface IVariableResolver {
	default String resolve(ConfijNode leaf) {
		return resolve(leaf, leaf.getValue());
	}

	String resolve(ConfijNode baseLeaf, String value);
}
