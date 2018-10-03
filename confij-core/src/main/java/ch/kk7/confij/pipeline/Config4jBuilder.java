package ch.kk7.confij.pipeline;

import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.format.ConfigFormat;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.validation.IValidator;
import ch.kk7.confij.validation.ServiceLoaderValidator;
import ch.kk7.confij.source.AnySource;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.defaults.DefaultSource;
import com.fasterxml.classmate.GenericType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Config4jBuilder<T> {
	// Someinterface config = Config4jBuilder.as(Someinterface.class)
	// .withSource(
	// 	  from("classpath://asd.yml").as(yaml())
	//	 .then().from("asd.xml").or().from("asd.xml")
	//	 .thenMaybe().from(":asdasd"))
	// .withReloadStrategy()
	// .withSettings(newDefaultSettings().withAllowNull(true))
	// .withValidator(...)
	// .withPrefix("/my/config")
	// .withLockHandler(...)
	// .build()
	private final Type forType;
	private List<ConfigSource> sources = new ArrayList<>();
	private IValidator validator = null;
	private FormatSettings formatSettings = FormatSettings.newDefaultSettings();

	protected Config4jBuilder(Type forType) {
		this.forType = forType;
	}

	public static <X> Config4jBuilder<X> of(Class<X> forClass) {
		return new Config4jBuilder<>(forClass);
	}

	public static <X> Config4jBuilder<X> of(GenericType<X> forType) {
		return new Config4jBuilder<>(forType);
	}

	public Config4jBuilder<T> withSource(String... sourceStr) {
		Stream.of(sourceStr)
				.map(AnySource::new)
				.forEachOrdered(s -> sources.add(s));
		return this;
	}

	public Config4jBuilder<T> withSource(ConfigSource... source) {
		sources.addAll(Arrays.asList(source));
		return this;
	}

	public Config4jBuilder<T> withValidator(IValidator validator) {
		this.validator = Objects.requireNonNull(validator, "validator");
		return this;
	}

	public Config4jBuilder<T> withoutValidator() {
		return withValidator(IValidator.NOOP);
	}

	public Config4jBuilder<T> withFormatSettings(FormatSettings formatSettings) {
		this.formatSettings = Objects.requireNonNull(formatSettings);
		return this;
	}

	public T build() {
		validator = Optional.ofNullable(validator)
				.orElseGet(ServiceLoaderValidator::new);
		ConfigBinder configBinder = new ConfigBinder();
		@SuppressWarnings("unchecked")
		ConfigBinding<T> configBinding = (ConfigBinding<T>) configBinder.toRootConfigBinding(forType);
		ConfigFormat configFormat = configBinding.describe(formatSettings);
		Config4jPipeline<T> pipeline = new Config4jPipeline<>(sources, new DefaultSource(), validator, configBinding, configFormat);
		return pipeline.build();
	}
}
