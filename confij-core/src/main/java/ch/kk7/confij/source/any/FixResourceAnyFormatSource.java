package ch.kk7.confij.source.any;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.resource.ConfijResource;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class FixResourceAnyFormatSource implements ServiceLoaderSourceAware, ConfijSource {
	@NonNull ConfijResource resource;

	@Override
	public void override(ConfijNode rootNode) {
		resource.read(rootNode)
				.forEach(content -> {
					ConfijFormat format = getDynamicFormat(content.getPath());
					FixResourceFixFormatSource.override(rootNode, format, content);
				});
	}
}
