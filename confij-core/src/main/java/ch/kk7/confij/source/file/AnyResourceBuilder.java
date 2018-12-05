package ch.kk7.confij.source.file;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.ConfigSourceBuilder;
import ch.kk7.confij.source.file.format.ConfijSourceFormat;
import ch.kk7.confij.source.file.resource.ConfijResourceProvider;
import com.google.auto.service.AutoService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@AutoService(ConfigSourceBuilder.class)
public class AnyResourceBuilder implements ConfigSourceBuilder {
	private final List<ConfijResourceProvider> supportedResources;
	private final List<ConfijSourceFormat> supportedFormats;

	public AnyResourceBuilder() {
		supportedResources = ServiceLoaderUtil.requireInstancesOf(ConfijResourceProvider.class);
		supportedFormats = ServiceLoaderUtil.requireInstancesOf(ConfijSourceFormat.class);
	}

	@Override
	public Optional<FixedResourceSource> fromURI(URI path) {
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
