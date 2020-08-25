package ch.kk7.confij.source.resource;

import java.net.URI;
import java.util.stream.Stream;

/**
 * A resource provider basically reads a string from anywhere given an URI.<br/>
 * <p>
 * If it should work as an {@code AnySource} it should be stateless (and receive all variable input from the input URI). Additionally it
 * should be registered as a {@code ConfijResourceProvider} {@code ServiceLoader}.
 */
public interface ConfijResourceProvider {
	/**
	 * stringify a resource
	 *
	 * @param path the URI to read from
	 * @return the string representation of the path's content
	 */
	Stream<String> read(URI path);

	/**
	 * Receive a "preview" on the URI to be processed. This resouce provider can choose to accept or reject processing it.
	 * This is most commonly decided based on the URI's scheme.
	 *
	 * @param path an URI to be processed later.
	 * @return true if this resouce provider accepts processing this URI (but it can still fail)
	 */
	boolean canHandle(URI path);
}
