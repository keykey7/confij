package ch.kk7.confij.source.any;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.Optional;

/**
 * An 'aggregating' configuration source provider: It will attempt to configure using any of the  {@code ConfigSourceBuilder}s registered as
 * ServiceLoaders.
 * It takes an URI-template as input. This template is resolved with the configuration-rootNode before processing it further.
 *
 * @see ConfijAnySource
 */
@Value
@NonFinal
public class AnySourceImpl implements ConfijSource {
	private static List<ConfijAnySource> sourceBuilders = ServiceLoaderUtil.requireInstancesOf(ConfijAnySource.class);
	@NonNull String pathTemplate;

	@Override
	public void override(ConfijNode rootNode) {
		String path = rootNode.resolve(pathTemplate);
		ConfijSource confijSource = sourceBuilders.stream()
				.map(sourceBulder -> sourceBulder.fromURI(path))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> {
					String addon = pathTemplate.equals(path) ? "" : " (resolved from '" + pathTemplate + "')";
					return new ConfijSourceException("The {} was unable to find a {} which can handle '{}'{}", this,
							ConfijAnySource.class.getSimpleName(), path, addon);
				});
		try {
			confijSource.override(rootNode);
		} catch (ConfijSourceException e) {
			throw new ConfijSourceException("Failed reading source from path `{}` using {} (" +
					"either fix the content of this source or write a new ServiceLoader implementing {}): {}", path, this,
					ConfijSource.class.getSimpleName(), e.getMessage(), e);
		}
	}
}
