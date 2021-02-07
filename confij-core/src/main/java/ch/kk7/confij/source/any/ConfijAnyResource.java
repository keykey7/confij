package ch.kk7.confij.source.any;

import ch.kk7.confij.source.resource.ConfijResource;

import java.util.Optional;

/**
 * A resource provider basically reads a string from anywhere given an URI.
 * <p>
 * If it should work as an {@code AnySource} it should be stateless (and receive all variable input from the input URI). Additionally it
 * should be registered as a {@code ConfijResourceProvider} {@code ServiceLoader}.
 */
@FunctionalInterface
public interface ConfijAnyResource {
	/**
	 * Receive a "preview" on the URI to be processed. This resouce provider can choose to accept or reject processing it.
	 * This is most commonly decided based on the URI's scheme.
	 *
	 * @param pathTemplate an URI to be processed later.
	 * @return a stream of strings if this resouce provider accepts processing this URI (but it can still fail),
	 * empty if the uri doesn't look like it could be processed
	 */
	Optional<? extends ConfijResource> maybeHandle(String pathTemplate);
}
