package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.tree.ConfijNode;
import ch.kk7.confij.validation.ConfijValidator;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class ConfijPipelineImpl<T> implements ConfijPipeline<T> {
	@NonNull
	private final List<ConfigSource> sources;
	@NonNull
	private final ConfigSource defaultSource;
	@NonNull
	private final ConfijValidator validator;
	@NonNull
	private final ConfigBinding<T> configBinding;
	@NonNull
	private final ConfigFormat format;

	protected ConfijNode newDefaultConfig() {
		ConfijNode defaultsOnly = ConfijNode.newRootFor(format);
		defaultSource.override(defaultsOnly);
		return defaultsOnly;
	}

	protected ConfijNode readConfigToNode() {
		ConfijNode rootNode = newDefaultConfig();
		for (ConfigSource source : sources) {
			source.override(rootNode);
			// always overriding with default source to make sure new
			// (optional) branches are filled with default values before
			// the next source might reference it...
			defaultSource.override(rootNode);
		}
		return rootNode;
	}

	protected T bind(ConfijNode rootNode) {
		return configBinding.bind(rootNode);
	}

	@Override
	public T build() {
		ConfijNode simpleConfig = readConfigToNode();
		T config = bind(simpleConfig);
		validator.validate(config);
		return config;
	}
}
