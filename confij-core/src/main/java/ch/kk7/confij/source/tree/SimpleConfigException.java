package ch.kk7.confij.source.tree;

import ch.kk7.confij.common.Config4jException;

public class SimpleConfigException extends Config4jException {
	public SimpleConfigException(String s, Object... args) {
		super(s, args);
	}
}
