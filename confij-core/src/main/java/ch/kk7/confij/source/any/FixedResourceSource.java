package ch.kk7.confij.source.any;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.format.ConfijSourceFormat;
import ch.kk7.confij.source.format.ConfijSourceFormatException;
import ch.kk7.confij.source.resource.ConfijResourceProvider;
import ch.kk7.confij.source.resource.ConfijSourceFetchingException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;

/**
 * An immutable mapping between a source (like a file) and a format (like properties).
 *
 * @see AnyResourceBuilder
 */
@Value
@NonFinal
public class FixedResourceSource implements ConfijSource {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FixedResourceSource.class);

	@NonNull URI path;
	@NonNull ConfijResourceProvider resource;
	@NonNull ConfijSourceFormat format;

	@Override
	public void override(ConfijNode rootNode) {
		final String configAsStr;
		try {
			configAsStr = resource.read(path);
		} catch (ConfijSourceException e) {
			throw new ConfijSourceFetchingException("failed to fetch raw configuration data from {} using {}", path, resource, e);
		}
		try {
			format.override(rootNode, configAsStr);
		} catch (ConfijSourceException e) {
			LOGGER.debug("processing configuration with {} failed. Source was: \n{}", format, configAsStr);
			throw new ConfijSourceFormatException(
					"Sucessfully read a configuration in this pipeline step, " +
							"but failed to apply it to the final configuration due to an invalid source format", e);
		}
	}
}
