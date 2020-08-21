package ch.kk7.confij.binding;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.values.DateTimeMapper;
import ch.kk7.confij.binding.values.DurationMapper;
import ch.kk7.confij.binding.values.EnumMapper;
import ch.kk7.confij.binding.values.ExplicitMapper;
import ch.kk7.confij.binding.values.OptionalMapper;
import ch.kk7.confij.binding.values.PeriodMapper;
import ch.kk7.confij.binding.values.PrimitiveMapperFactory;
import ch.kk7.confij.binding.values.SoloConstructorMapper;
import ch.kk7.confij.binding.values.StaticFunctionMapper;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.AnnotationUtil.AnnonResponse;
import ch.kk7.confij.common.ClassToImplCache;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * While traversing the tree of interfaces defining the configuration, this represents a state at a given node.
 * It is an aggregated state over all the parent nodes, usually considering the annotations up to a node.
 */
@Value
@With
@NonFinal
public class BindingContext {
	ValueMapperFactory forcedMapperFactory;
	@Getter
	@NonNull
	List<ValueMapperFactory> mapperFactories;
	@NonNull
	Map<Class<? extends ValueMapperFactory>, Annotation> factoryConfigs;

	public BindingContext(ValueMapperFactory forcedMapperFactory, @NonNull List<ValueMapperFactory> mapperFactories,
			@NonNull Map<Class<? extends ValueMapperFactory>, Annotation> factoryConfigs) {
		this.forcedMapperFactory = forcedMapperFactory;
		this.mapperFactories = Collections.unmodifiableList(mapperFactories);
		this.factoryConfigs = Collections.unmodifiableMap(factoryConfigs);
	}

	public static BindingContext newDefaultContext() {
		List<ValueMapperFactory> mapperFactories = Arrays.asList(ExplicitMapper.forString(), new PrimitiveMapperFactory(),
				new OptionalMapper(), ExplicitMapper.forFile(), ExplicitMapper.forPath(), new EnumMapper(), new DurationMapper(),
				new PeriodMapper(), new DateTimeMapper(), new StaticFunctionMapper(), new SoloConstructorMapper());
		return new BindingContext(null, mapperFactories, Collections.emptyMap());
	}

	public Optional<ValueMapperFactory> getForcedMapperFactory() {
		return Optional.ofNullable(forcedMapperFactory);
	}

	public BindingContext withMapperFactory(ValueMapperFactory valueMapperFactory) {
		// always add at the beginning
		List<ValueMapperFactory> factories = new ArrayList<>();
		factories.add(valueMapperFactory);
		factories.addAll(mapperFactories);
		return withMapperFactories(factories);
	}

	protected BindingContext withMapperFactoryFor(ValueMapper valueMapper, boolean forced) {
		Class<? extends ValueMapperFactory> clazz = valueMapper.value();
		ValueMapperFactory mapperFactory = ClassToImplCache.getInstance(clazz, ValueMapperFactory.class);
		if (forced) {
			return withForcedMapperFactory(mapperFactory);
		}
		return withMapperFactory(mapperFactory);
	}

	public Optional<Annotation> getFactoryConfigFor(Class<? extends ValueMapperFactory> forClass) {
		return Optional.ofNullable(factoryConfigs.get(forClass));
	}

	protected BindingContext withFactoryConfigFor(Class<? extends ValueMapperFactory> forClass, Annotation declaringAnnotation) {
		Map<Class<? extends ValueMapperFactory>, Annotation> factoryConfigs = new HashMap<>(this.factoryConfigs);
		factoryConfigs.put(forClass, declaringAnnotation);
		return withFactoryConfigs(factoryConfigs);
	}

	public BindingContext settingsFor(AnnotatedElement element, boolean forced) {
		Optional<AnnonResponse<ValueMapper>> declaration = AnnotationUtil.findAnnotationAndDeclaration(element, ValueMapper.class);
		if (declaration.isPresent()) {
			AnnonResponse<ValueMapper> response = declaration.get();
			ValueMapper valueMapper = response.getAnnotationType();
			return withFactoryConfigFor(valueMapper.value(), response.getDeclaredAnnotation()).withMapperFactoryFor(valueMapper, forced);
		}
		return this;
	}
}
