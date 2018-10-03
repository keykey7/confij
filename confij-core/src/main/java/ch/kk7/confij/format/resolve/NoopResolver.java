package ch.kk7.confij.format.resolve;

import ch.kk7.confij.source.simple.SimpleConfig;

public class NoopResolver implements IVariableResolver {
	@Override
	public String resolve(SimpleConfig baseLeaf, String value) {
		return value;
	}
}
