package ch.kk7.confij.source.any;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.resource.ConfijResource;
import ch.kk7.confij.source.resource.ConfijResource.ResourceContent;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

/**
 * An immutable mapping between a source (like a file) and a format (like properties).
 *
 * @see AnyResourceAnyFormatAnySource
 */
@Value
@NonFinal
public class FixResourceFixFormatSource implements ConfijSource {
	private static final ConfijLogger LOGGER = ConfijLogger.getLogger(FixResourceFixFormatSource.class);
	@NonNull ConfijResource resource;
	@NonNull ConfijFormat format;

	@Override
	public void override(ConfijNode rootNode) {
		resource.read(rootNode)
				.forEach(content -> override(rootNode, format, content));
	}

	public static void override(ConfijNode rootNode, ConfijFormat format, ResourceContent content) {
		try {
			format.override(rootNode, content.getContent());
		} catch (ConfijSourceException e) {
			LOGGER.debug("processing configuration with {} failed. Source was: \n{}", format, content);
			throw new ConfijSourceException("Sucessfully read a configuration in this pipeline step, " +
					"but failed to apply it to the final configuration due to an invalid source format: {}", e.getMessage(), e);
		}
	}
}
