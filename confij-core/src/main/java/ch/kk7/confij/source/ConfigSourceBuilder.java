package ch.kk7.confij.source;

import java.net.URI;
import java.util.Optional;

public interface ConfigSourceBuilder {
	Optional<? extends ConfigSource> fromURI(URI path);
}
