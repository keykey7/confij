package ch.kk7.confij.source.any;

import ch.kk7.confij.common.ServiceLoaderUtil;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.resource.ConfijResource;

import java.util.List;
import java.util.Optional;

public interface ServiceLoaderSourceAware {
	List<ConfijAnyResource> supportedResources = ServiceLoaderUtil.requireInstancesOf(ConfijAnyResource.class);
	List<ConfijAnyFormat> supportedFormats = ServiceLoaderUtil.requireInstancesOf(ConfijAnyFormat.class);

	default ConfijResource getDynamicResource(String pathTemplate) {
		return supportedResources.stream()
				.map(r -> r.maybeHandle(pathTemplate))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new ConfijSourceException("failed to find a {} who can handle '{}', available are: {}",
						ConfijAnyResource.class.getSimpleName(), pathTemplate, supportedResources));
	}

	default ConfijFormat getDynamicFormat(String pathTemplate) {
		// TODO: one can think of cases where the resource might give an additional hint towards the format
		//       like a glob 'someFile.*' where the resource could figure out it matches to a yaml + a properties for example
		return supportedFormats.stream()
				.map(s -> s.maybeHandle(pathTemplate))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new ConfijSourceException("failed to find a {} who can handle '{}', available are: {}",
						ConfijAnyFormat.class.getSimpleName(), pathTemplate, supportedFormats));
	}
}
