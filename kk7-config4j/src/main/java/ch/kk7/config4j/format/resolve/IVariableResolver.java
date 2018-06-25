package ch.kk7.config4j.format.resolve;

import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigLeaf;

public interface IVariableResolver {
	default String resolve(SimpleConfigLeaf leaf) {
		return resolve(leaf, leaf.get());
	}

	String resolve(SimpleConfig baseLeaf, String value);
}
