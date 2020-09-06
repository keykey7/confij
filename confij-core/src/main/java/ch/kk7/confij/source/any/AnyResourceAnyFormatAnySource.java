package ch.kk7.confij.source.any;

import ch.kk7.confij.common.ServiceLoaderPriority;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.resource.ConfijResource;
import com.google.auto.service.AutoService;
import lombok.ToString;

import java.util.Optional;

/**
 * An 'any'-resource which dynamically combines a stringifyable source (like a file) with a format (like properties).
 *
 * @see ConfijAnyResource
 * @see ConfijFormat
 */
@ToString
@AutoService(ConfijAnySource.class)
public class AnyResourceAnyFormatAnySource implements ConfijAnySource, ServiceLoaderPriority, ServiceLoaderSourceAware {
	@Override
	public int getPriority() {
		return ServiceLoaderPriority.DEFAULT_PRIORITY - 100;
	}

	@Override
	public Optional<ConfijSource> fromURI(String pathTemplate) {
		// TODO: currently crashes if not matching... debatable behaviour as it doesn't matche the ConfijAnySource contract
		ConfijResource resource = getDynamicResource(pathTemplate);
		ConfijFormat format = getDynamicFormat(pathTemplate);
		return Optional.of(new FixResourceFixFormatSource(resource, format));
	}
}
