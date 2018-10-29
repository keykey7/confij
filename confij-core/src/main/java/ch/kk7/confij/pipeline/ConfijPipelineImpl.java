package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.simple.ConfijNode;
import ch.kk7.confij.validation.IValidator;
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
	private final IValidator validator;
	@NonNull
	private final ConfigBinding<T> configBinding;
	@NonNull
	private final ConfigFormat format;

	protected ConfijNode newDefaultConfig() {
		ConfijNode defaultsOnly = ConfijNode.newRootFor(format);
		defaultSource.override(defaultsOnly);
		return defaultsOnly;
	}

	protected ConfijNode readSimpleConfig() {
		ConfijNode simpleConfig = newDefaultConfig();
		for (ConfigSource source : sources) {
			source.override(simpleConfig);
			// always overriding with default source to make sure new
			// (optional) branches are filled with default values before
			// the next source might reference it...
			defaultSource.override(simpleConfig);
		}
		return simpleConfig;
	}

	protected T bind(ConfijNode simpleConfig) {
		return configBinding.bind(simpleConfig);
	}

	@Override
	public T build() {
		ConfijNode simpleConfig = readSimpleConfig();
		T config = bind(simpleConfig);
		validator.validate(config);
		return config;
	}
}
