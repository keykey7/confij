package ch.kk7.confij.binding;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.values.ValueMapperFactory;
import ch.kk7.confij.binding.values.ValueMapperInstance;
import ch.kk7.confij.binding.values.DateTimeMapper;
import ch.kk7.confij.binding.values.DurationMapper;
import ch.kk7.confij.binding.values.EnumMapper;
import ch.kk7.confij.binding.values.ExplicitMapper;
import ch.kk7.confij.binding.values.PeriodMapper;
import ch.kk7.confij.binding.values.PrimitiveMapperFactory;
import ch.kk7.confij.binding.values.SoloConstructorMapper;
import ch.kk7.confij.binding.values.StaticFunctionMapper;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.AnnotationUtil.AnnonResponse;
import ch.kk7.confij.format.FormatSettings.LazyClassToImplCache;
import lombok.AllArgsConstructor;
import lombok.NonNull;

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
 * considered when parsing the configuration types.
 */
@AllArgsConstructor
public class BindingSettings {
	private final ValueMapperFactory forcedMapperFactory;
	@NonNull
	private final List<ValueMapperFactory> mapperFactories;
	@NonNull
	private final Map<Class<? extends ValueMapperFactory>, Annotation> factoryConfigs;
	@NonNull
	private final LazyClassToImplCache implCache;

	public static BindingSettings newDefaultSettings() {
		List<ValueMapperFactory> mapperFactories = Arrays.asList(
				ExplicitMapper.forString(),
				new PrimitiveMapperFactory(),
				ExplicitMapper.forFile(),
				ExplicitMapper.forPath(),
				new EnumMapper(),
				new DurationMapper(),
				new PeriodMapper(),
				new DateTimeMapper(),
				new StaticFunctionMapper(),
				new SoloConstructorMapper());
		return new BindingSettings(null, Collections.unmodifiableList(mapperFactories), Collections.emptyMap(), new LazyClassToImplCache());
	}

	public BindingSettings withValueMapperFactories(List<ValueMapperFactory> mapperFactories) {
		return new BindingSettings(forcedMapperFactory, Collections.unmodifiableList(mapperFactories), factoryConfigs, implCache);
	}

	public BindingSettings addValueMapperFactory(ValueMapperFactory valueMapperFactory) {
		// always add at the beginning
		List<ValueMapperFactory> factories = new ArrayList<>();
		factories.add(valueMapperFactory);
		factories.addAll(mapperFactories);
		return withValueMapperFactories(factories);
	}

	public <T> BindingSettings addValueMapper(ValueMapperInstance<T> valueMapper, Class<T> forClass) {
		return addValueMapperFactory(ValueMapperFactory.forClass(valueMapper, forClass));
	}

	public Optional<ValueMapperFactory> getForcedMapperFactory() {
		return Optional.ofNullable(forcedMapperFactory);
	}

	public Optional<Annotation> getFactoryConfigFor(Class<? extends ValueMapperFactory> forClass) {
		return Optional.ofNullable(factoryConfigs.get(forClass));
	}

	@NonNull
	public List<ValueMapperFactory> getMapperFactories() {
		return mapperFactories;
	}

	public BindingSettings settingsFor(AnnotatedElement element) {
		List<ValueMapperFactory> mapperFactories = this.mapperFactories;
		Map<Class<? extends ValueMapperFactory>, Annotation> factoryConfigs = this.factoryConfigs;

		// handle value mapping factories
		ValueMapperFactory forcedMapperFactory = null;
		Optional<AnnonResponse<ValueMapper>> declaration = AnnotationUtil.findAnnotationAndDeclaration(element, ValueMapper.class);
		if (declaration.isPresent()) {
			AnnonResponse<ValueMapper> response = declaration.get();
			ValueMapper valueMapper = response.getAnnotationType();
			Class<? extends ValueMapperFactory> clazz = valueMapper.value();
			ValueMapperFactory mapperClass = implCache.getInstance(clazz, ValueMapperFactory.class);
			if (valueMapper.force()) {
				forcedMapperFactory = mapperClass;
			} else {
				mapperFactories = new ArrayList<>();
				mapperFactories.add(mapperClass);
				mapperFactories.addAll(this.mapperFactories);
			}
			factoryConfigs = new HashMap<>(this.factoryConfigs);
			factoryConfigs.put(clazz, response.getDeclaredAnnotation());
		}
		return new BindingSettings(forcedMapperFactory, mapperFactories, factoryConfigs, implCache);
	}
}
