package org.junit;

import lombok.experimental.UtilityClass;

/**
 * @deprecated workaround as jgit.http requires legcay junit4 stuff
 */
@Deprecated
@UtilityClass
public class Assert {
	public void assertFalse(String msg, boolean expr) {
		//noinspection SimplifiableAssertion
		assertTrue(msg, !expr);
	}

	public void assertTrue(String msg, boolean expr) {
		if (!expr) {
			throw new IllegalStateException("assertion failed: " + msg);
		}
	}
}
