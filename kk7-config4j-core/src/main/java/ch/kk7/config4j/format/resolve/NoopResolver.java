package ch.kk7.config4j.format.resolve;

import ch.kk7.config4j.source.simple.SimpleConfig;

public class NoopResolver implements IVariableResolver {
	@Override
	public String resolve(SimpleConfig baseLeaf, String value) {
		return value;
	}
}
