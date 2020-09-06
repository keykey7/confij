package ch.kk7.confij.source.any;

import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.format.ConfijFormat;
import ch.kk7.confij.source.resource.ConfijResource;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.stream.Stream;

@Value
@NonFinal
public class FixResourceAnyFormatSource implements ServiceLoaderSourceAware, ConfijSource {
	@NonNull String pathTemplate;
	@NonNull ConfijResource resource;

	@Override
	public void override(ConfijNode rootNode) {
		String path = rootNode.resolve(pathTemplate);
		ConfijFormat format = getDynamicFormat(path);
		Stream<String> configsAsStr = resource.read(rootNode);
		configsAsStr.forEach(configAsStr -> format.override(rootNode, configAsStr));
	}
}
