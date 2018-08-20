package ch.kk7.config4j.pipeline;

import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.leaf.mapper.DefaultValueMapperFactory;
import ch.kk7.config4j.binding.leaf.mapper.ValueMapper;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.format.validation.IValidator;
import ch.kk7.config4j.format.validation.NotNullValidator;
import ch.kk7.config4j.source.ConfigSource;
import ch.kk7.config4j.source.SourceBuilder;
import ch.kk7.config4j.source.defaults.DefaultSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
	private final Class<T> forClass;
	private List<ConfigSource> sources = new ArrayList<>();
	private DefaultValueMapperFactory valueMapperFactory = new DefaultValueMapperFactory();
	private List<IValidator> validators = new ArrayList<>(Collections.singleton(new NotNullValidator()));
	private FormatSettings formatSettings = FormatSettings.newDefaultSettings();

	protected Config4jBuilder(Class<T> forClass) {
		this.forClass = forClass;
	}

	public static <X> Config4jBuilder<X> of(Class<X> forClass) {
		return new Config4jBuilder<>(forClass);
	}

	public Config4jBuilder<T> withSource(String... sourceStr) {
		Stream.of(sourceStr)
				.map(SourceBuilder::of)
				.forEachOrdered(s -> sources.add(s));
		return this;
	}

	public Config4jBuilder<T> withSource(ConfigSource... source) {
		sources.addAll(Arrays.asList(source));
		return this;
	}

	public <X> Config4jBuilder<T> withValueMapping(Class<X> forClass, ValueMapper<X> mapping) {
		valueMapperFactory.withMapping(forClass, mapping);
		return this;
	}

	public Config4jBuilder<T> withValidator(IValidator validator) {
		validators.add(Objects.requireNonNull(validator, "validator"));
		return this;
	}

	public Config4jBuilder<T> withFormatSettings(FormatSettings formatSettings) {
		this.formatSettings = Objects.requireNonNull(formatSettings);
		return this;
	}

	public T build() {
		ConfigBinder configBinder = new ConfigBinder(valueMapperFactory);
		ConfigBinding<T> configBinding = configBinder.toConfigBinding(forClass);
		ConfigFormat configFormat = configBinding.describe(formatSettings);
		Config4jPipeline<T> pipeline = new Config4jPipeline<>(sources, new DefaultSource(), validators, configBinding, configFormat);
		return pipeline.build();
	}
}
