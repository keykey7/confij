package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.validation.IValidator;
import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.simple.SimpleConfig;

import java.util.List;
import java.util.Objects;

public class Config4jPipeline<T> {
	private final List<ConfigSource> sources;
	private final ConfigSource defaultSource;
	private final IValidator validator;
	private final ConfigBinding<T> configBinding;
	private final ConfigFormat format;

	public Config4jPipeline(List<ConfigSource> sources, ConfigSource defaultSource, IValidator validator,
			ConfigBinding<T> configBinding, ConfigFormat format) {
		this.sources = Objects.requireNonNull(sources);
		this.defaultSource = Objects.requireNonNull(defaultSource);
		this.configBinding = Objects.requireNonNull(configBinding);
		this.format = Objects.requireNonNull(format);
		this.validator = Objects.requireNonNull(validator);
	}

	protected SimpleConfig newDefaultConfig() {
		SimpleConfig defaultsOnly = SimpleConfig.newRootFor(format);
		defaultSource.override(defaultsOnly);
		return defaultsOnly;
	}

	protected SimpleConfig readSimpleConfig() {
		SimpleConfig simpleConfig = newDefaultConfig();
		for (ConfigSource source : sources) {
			source.override(simpleConfig);
			// always overriding with default source to make sure new
			// (optional) branches are filled with default values before
			// the next source might reference it...
			defaultSource.override(simpleConfig);
		}
		return simpleConfig;
	}

	protected T bind(SimpleConfig simpleConfig) {
		return configBinding.bind(simpleConfig);
	}

	public T build() {
		SimpleConfig simpleConfig = readSimpleConfig();
		T config = bind(simpleConfig);
		validator.validate(config);
		return config;
	}
}
