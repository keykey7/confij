package ch.kk7.confij.format.resolve;

import ch.kk7.confij.source.tree.ConfijNode;

public class NoopResolver implements IVariableResolver {
	@Override
	public String resolve(ConfijNode baseLeaf, String value) {
		return value;
	}
}
