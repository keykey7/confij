package ch.kk7.confij.source.tree;

import ch.kk7.confij.common.ConfijException;

public class SimpleConfigException extends ConfijException {
	public SimpleConfigException(String s, Object... args) {
		super(s, args);
	}
}
