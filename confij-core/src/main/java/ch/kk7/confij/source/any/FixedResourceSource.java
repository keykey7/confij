package ch.kk7.confij.source.any;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.format.ConfijSourceFormat;
import ch.kk7.confij.source.format.ConfijSourceFormatException;
import ch.kk7.confij.source.resource.ConfijResourceProvider;
import ch.kk7.confij.source.resource.ConfijSourceFetchingException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.stream.Stream;

/**
 * An immutable mapping between a source (like a file) and a format (like properties).
 *
 * @see AnyResourceBuilder
 */
@Value
@NonFinal
public class FixedResourceSource implements ConfijSource {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FixedResourceSource.class);
	@NonNull ConfijSourceBuilder.URIish path;
	@NonNull ConfijResourceProvider resource;
	@NonNull ConfijSourceFormat format;

	@Override
	public void override(ConfijNode rootNode) {
		final Stream<String> configsAsStr;
		try {
			configsAsStr = resource.read(path);
		} catch (ConfijSourceException e) {
			throw new ConfijSourceFetchingException("failed to fetch raw configuration data from '{}' using {}", path, resource, e);
		}
		configsAsStr.forEach(configAsStr -> {
			try {
				format.override(rootNode, configAsStr);
			} catch (ConfijSourceException e) {
				LOGGER.debug("processing configuration with {} failed. Source was: \n{}", format, configAsStr);
				throw new ConfijSourceFormatException("Sucessfully read a configuration in this pipeline step, " +
						"but failed to apply it to the final configuration due to an invalid source format: {}", e.getMessage(), e);
			}
		});
	}
}
