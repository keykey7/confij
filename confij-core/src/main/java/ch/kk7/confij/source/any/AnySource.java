package ch.kk7.confij.source.any;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.template.ValueResolver;
import ch.kk7.confij.tree.ConfijNode;
import lombok.Data;
import lombok.ToString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An 'aggregating' configuration source provider: It will attempt to configure using any of the  {@code ConfigSourceBuilder}s registered as
 * ServiceLoaders.
 * It takes an URI-template as input. This template is resolved with the configuration-rootNode before processing it further.
 *
 * @see ConfijSourceBuilder
 */
@Data
public class AnySource implements ConfijSource {
	@ToString.Exclude
	private final List<ConfijSourceBuilder> sourceBuilders;
	private final String pathTemplate;

	public AnySource(String pathTemplate) {
		this.pathTemplate = Objects.requireNonNull(pathTemplate);
		sourceBuilders = ServiceLoaderUtil.requireInstancesOf(ConfijSourceBuilder.class);
	}

	protected ValueResolver getResolver(ConfijNode rootNode) {
		return rootNode.getConfig()
				.getNodeBindingContext()
				.getValueResolver();
	}

	protected URI resolveUri(ConfijNode rootNode) {
		String actualPath = getResolver(rootNode).resolveValue(rootNode, pathTemplate);
		try {
			return new URI(actualPath);
		} catch (URISyntaxException e) {
			throw new ConfijSourceException("The {} failed to resolve the path-template '{}' into a valid URI", this, pathTemplate, e);
		}
	}

	@Override
	public void override(ConfijNode rootNode) {
		URI path = resolveUri(rootNode);
		ConfijSource confijSource = sourceBuilders.stream()
				.map(sourceBulder -> sourceBulder.fromURI(path))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> {
					String addon = pathTemplate.equals(path.toString()) ? "" : " (resolved from '" + pathTemplate + "')";
					return new ConfijSourceException("The {} was unable to find a {} which can handle '{}'{}", this,
							ConfijSourceBuilder.class.getSimpleName(), path, addon);
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
