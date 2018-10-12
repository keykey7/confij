package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.reload.ConfijReloader;
import ch.kk7.confij.reload.ScheduledReloader;
import ch.kk7.confij.source.AnySource;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.defaults.DefaultSource;
import ch.kk7.confij.validation.IValidator;
import ch.kk7.confij.validation.ServiceLoaderValidator;
import com.fasterxml.classmate.GenericType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ConfijBuilder<T> {
	private final Type forType;
	private List<ConfigSource> sources = new ArrayList<>();
	private IValidator validator = null;
	private FormatSettings formatSettings = FormatSettings.newDefaultSettings();
	private ConfijReloader<T> reloader = null;

	protected ConfijBuilder(Type forType) {
		this.forType = forType;
	}

	public static <X> ConfijBuilder<X> of(Class<X> forClass) {
		return new ConfijBuilder<>(forClass);
	}

	public static <X> ConfijBuilder<X> of(GenericType<X> forType) {
		return new ConfijBuilder<>(forType);
	}

	public ConfijBuilder<T> withSource(String... sourceStr) {
		Stream.of(sourceStr)
				.map(AnySource::new)
				.forEachOrdered(s -> sources.add(s));
		return this;
	}

	public ConfijBuilder<T> withSource(ConfigSource... source) {
		sources.addAll(Arrays.asList(source));
		return this;
	}

	public ConfijBuilder<T> withValidator(IValidator validator) {
		this.validator = Objects.requireNonNull(validator, "validator");
		return this;
	}

	public ConfijBuilder<T> withoutValidator() {
		return withValidator(IValidator.NOOP);
	}

	public ConfijBuilder<T> withFormatSettings(FormatSettings formatSettings) {
		this.formatSettings = Objects.requireNonNull(formatSettings, "formatSettings");
		return this;
	}

	public ConfijBuilder<T> withReloader(ConfijReloader<T> reloader) {
		this.reloader = Objects.requireNonNull(reloader, "reloader");
		return this;
	}

	protected ConfijPipeline<T> buildPipeline() {
		validator = Optional.ofNullable(validator)
				.orElseGet(ServiceLoaderValidator::new);
		ConfigBinder configBinder = new ConfigBinder();
		@SuppressWarnings("unchecked")
		ConfigBinding<T> configBinding = (ConfigBinding<T>) configBinder.toRootConfigBinding(forType);
		ConfigFormat configFormat = configBinding.describe(formatSettings);
		return new ConfijPipelineImpl<>(sources, new DefaultSource(), validator, configBinding, configFormat);
	}

	public T build() {
		return buildPipeline().build();
	}

	public ConfijReloader<T> buildReloadable() {
		reloader = Optional.ofNullable(reloader)
				.orElseGet(ScheduledReloader::new);
		reloader.initialize(buildPipeline());
		return reloader;
	}
}
