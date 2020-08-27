package ch.kk7.confij.source.any;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceBuilder;
import ch.kk7.confij.source.format.ConfijSourceFormat;
import ch.kk7.confij.source.resource.ConfijResourceProvider;
import com.google.auto.service.AutoService;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

/**
 * An 'any'-resource which dynamically combines a stringifyable source (like a file) with a format (like properties).
 *
 * @see ConfijResourceProvider
 * @see ConfijSourceFormat
 */
@ToString
@AutoService(ConfijSourceBuilder.class)
public class AnyResourceBuilder implements ConfijSourceBuilder {
	private final List<ConfijResourceProvider> supportedResources;
	private final List<ConfijSourceFormat> supportedFormats;

	public AnyResourceBuilder() {
		supportedResources = ServiceLoaderUtil.requireInstancesOf(ConfijResourceProvider.class);
		supportedFormats = ServiceLoaderUtil.requireInstancesOf(ConfijSourceFormat.class);
	}

	@Override
	public Optional<ConfijSource> fromURI(URIish path) {
		Optional<ConfijResourceProvider> resource = supportedResources.stream()
				.filter(r -> r.canHandle(path))
				.findFirst();
		if (!resource.isPresent()) {
			return Optional.empty();
		}
		return supportedFormats.stream()
				.filter(r -> r.canHandle(path))
				.findFirst()
				.map(format -> new FixedResourceSource(path, resource.get(), format));
	}
}
