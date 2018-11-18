package ch.kk7.confij.source.file;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.file.format.ConfijSourceFormat;
import ch.kk7.confij.source.file.resource.ConfijResourceProvider;
import ch.kk7.confij.source.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
public class FixedResourceSource implements ConfigSource {
	@NonNull
	private final URI path;
	@NonNull
	private final ConfijResourceProvider resource;
	@NonNull
	private final ConfijSourceFormat format;

	@Override
	public void override(ConfijNode rootNode) {
		String configAsStr = resource.read(path);
		format.override(rootNode, configAsStr);
	}
}
