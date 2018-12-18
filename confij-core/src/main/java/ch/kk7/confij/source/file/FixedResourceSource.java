package ch.kk7.confij.source.file;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.file.format.ConfijSourceFormat;
import ch.kk7.confij.source.file.resource.ConfijResourceProvider;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;

@Value
@NonFinal
public class FixedResourceSource implements ConfigSource {
	@NonNull URI path;
	@NonNull ConfijResourceProvider resource;
	@NonNull ConfijSourceFormat format;

	@Override
	public void override(ConfijNode rootNode) {
		String configAsStr = resource.read(path);
		format.override(rootNode, configAsStr);
	}
}
