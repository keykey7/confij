package ch.kk7.confij.source;

import java.net.URI;
import java.util.Optional;

public interface ConfijSourceBuilder {
	Optional<ConfijSource> fromURI(URI path);
}
