package ch.kk7.confij.source.simple;

import ch.kk7.confij.common.Config4jException;

import java.net.URI;

public class SimpleConfigException extends Config4jException {
	public SimpleConfigException(String s, Object... args) {
		super(s, args);
	}

	public static SimpleConfigException newResolvePathException(URI base, Object conflict, Object expected) {
		return new SimpleConfigException("unable to resolveString config path on '{}', cannot resolveString '{}', allowed are: {}", base, conflict,
				expected);
	}

	public static String classOf(Object o) {
		return o == null ? null : o.getClass()
				.getName();
	}
}
