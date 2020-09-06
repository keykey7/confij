package ch.kk7.confij.source.any;

import ch.kk7.confij.source.format.ConfijFormat;

import java.util.Optional;

public interface ConfijAnyFormat {
	Optional<ConfijFormat> maybeHandle(String pathTemplate);
}
