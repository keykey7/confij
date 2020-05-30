package ch.kk7.confij.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class is used to pass full generics type information, and
 * avoid problems with type erasure (that basically removes most
 * usable type references from runtime Class objects).
 * It is based on ideas from
 * <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
 * >http://gafter.blogspot.com/2006/12/super-type-tokens.html</a>,
 *<p>
 * Usage is by sub-classing: here is one way to instantiate reference
 * to generic type <code>List&lt;Integer&gt;</code>:
 *<pre>
 *  GenericType type = new GenericType&lt;List&lt;Integer&gt;&gt;() { };
 *</pre>
 */
@SuppressWarnings("java:S2176")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GenericType<T> {
	// we hide the fasterxml GenericType to not expose "internal" classes in the API
}
