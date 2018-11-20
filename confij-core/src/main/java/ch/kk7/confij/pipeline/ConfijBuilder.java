package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.format.resolve.NoopResolver;
import ch.kk7.confij.format.resolve.VariableResolver;
import ch.kk7.confij.pipeline.reload.ConfijReloader;
import ch.kk7.confij.pipeline.reload.ScheduledReloader;
import ch.kk7.confij.source.AnySource;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.defaults.DefaultSource;
import ch.kk7.confij.validation.ConfijValidator;
import ch.kk7.confij.validation.ServiceLoaderValidator;
import com.fasterxml.classmate.GenericType;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConfijBuilder<T> {
	private final Type forType;
	private final List<ConfigSource> sources = new ArrayList<>();
	private ConfijValidator validator = null;
	private FormatSettings formatSettings = FormatSettings.newDefaultSettings();
	private BindingContext bindingContext = null;
	private ConfijReloader<T> reloader = null;

	protected ConfijBuilder(@NonNull Type forType) {
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
				.forEachOrdered(sources::add);
		return this;
	}

	public ConfijBuilder<T> withSource(ConfigSource... source) {
		sources.addAll(Arrays.asList(source));
		return this;
	}

	public ConfijBuilder<T> withValidator(@NonNull ConfijValidator validator) {
		this.validator = validator;
		return this;
	}

	public ConfijBuilder<T> withoutValidator() {
		return withValidator(ConfijValidator.NOOP);
	}

	public ConfijBuilder<T> withFormatSettings(FormatSettings formatSettings) {
		this.formatSettings = formatSettings;
		return this;
	}

	public ConfijBuilder<T> withTemplating(@NonNull VariableResolver variableResolver) {
		formatSettings = formatSettings.withVariableResolver(variableResolver);
		return this;
	}

	public ConfijBuilder<T> withoutTemplating() {
		return withTemplating(new NoopResolver());
	}

	@NonNull
	protected BindingContext getBindingContext() {
		if (bindingContext == null) {
			bindingContext = BindingContext.newDefaultContext();
		}
		return bindingContext;
	}

	public ConfijBuilder<T> bindingContext(BindingContext bindingContext) {
		if (this.bindingContext != null) {
			throw new IllegalStateException("unsafe usage of BindingSettings after it has been modified already");
		}
		this.bindingContext = bindingContext;
		return this;
	}

	public ConfijBuilder<T> withValueMapperFactory(ValueMapperFactory valueMapperFactory) {
		this.bindingContext = getBindingContext().withMapperFactory(valueMapperFactory);
		return this;
	}

	public <I> ConfijBuilder<T> withValueMapperForClass(ValueMapperInstance<I> valueMapper, Class<I> forClass) {
		return withValueMapperFactory(ValueMapperFactory.forClass(valueMapper, forClass));
	}

	public ConfijBuilder<T> withReloader(@NonNull ConfijReloader<T> reloader) {
		this.reloader = reloader;
		return this;
	}

	protected ConfijPipeline<T> buildPipeline() {
		validator = Optional.ofNullable(validator)
				.orElseGet(ServiceLoaderValidator::new);
		ConfigBinder configBinder = new ConfigBinder();
		@SuppressWarnings("unchecked")
		ConfigBinding<T> configBinding = (ConfigBinding<T>) configBinder.toRootConfigBinding(forType, getBindingContext());
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
