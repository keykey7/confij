package ch.kk7.confij.binding;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.annotation.ValueMapperFactory;
import ch.kk7.confij.binding.leaf.IValueMapperFactory;
import ch.kk7.confij.binding.leaf.mapper.EnumMapper;
import ch.kk7.confij.binding.leaf.mapper.ExplicitMapperFactory;
import ch.kk7.confij.binding.leaf.mapper.PrimitiveMapperFactory;
import ch.kk7.confij.binding.leaf.mapper.SoloConstructorMapper;
import ch.kk7.confij.binding.leaf.mapper.StaticFunctionMapper;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.format.FormatSettings.LazyClassToImplCache;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BindingSettings {
	private final IValueMapperFactory forcedMapperFactory;
	private final List<IValueMapperFactory> mapperFactories;
	private final LazyClassToImplCache implCache;

	protected BindingSettings(IValueMapperFactory forcedMapperFactory, List<IValueMapperFactory> mapperFactories, LazyClassToImplCache implCache) {
		this.forcedMapperFactory = forcedMapperFactory;
		this.mapperFactories = Collections.unmodifiableList(mapperFactories);
		this.implCache = Objects.requireNonNull(implCache);
	}

	public static BindingSettings newDefaultSettings() {
		List<IValueMapperFactory> mapperFactories = Arrays.asList(new PrimitiveMapperFactory(), new ExplicitMapperFactory(),
				new EnumMapper.EnumMapperFactory(), new StaticFunctionMapper.StaticFunctionMapperFactory(),
				new SoloConstructorMapper.SoloConstructorMapperFactory());
		return new BindingSettings(null, mapperFactories, new LazyClassToImplCache());
	}

	public Optional<IValueMapperFactory> getForcedMapperFactory() {
		return Optional.ofNullable(forcedMapperFactory);
	}

	public List<IValueMapperFactory> getMapperFactories() {
		return mapperFactories;
	}

	public BindingSettings settingsFor(AnnotatedElement element) {
		List<IValueMapperFactory> mapperFactories = new ArrayList<>(this.mapperFactories);

		// handle value mapping of the next type
		final IValueMapperFactory forcedMapper = AnnotationUtil.findAnnotation(element, ValueMapper.class).map(ValueMapper::value)
				.map(implCache::getInstance) // TODO: a new instance would be safer...
				.map(x -> (IValueMapperFactory) bindingType -> Optional.of(x))
				.orElse(null);

		// handle value mapping factories
		Optional<ValueMapperFactory> annon = AnnotationUtil.findAnnotation(element, ValueMapperFactory.class);
		IValueMapperFactory forcedMapperFactory = annon.filter(ValueMapperFactory::force)
				.map(ValueMapperFactory::value)
				.map(implCache::getInstance)
				.map(x -> {
					if (forcedMapper != null) {
						throw new BindingException("invalid annotation with mutually exclusive {} and {}", ValueMapper.class, ValueMapperFactory.class);
					}
					return (IValueMapperFactory) x;
				})
				.orElse(forcedMapper);

		// handle value optional mapping factories
		annon.filter(valueMapperFactory -> !valueMapperFactory.force())
				.map(ValueMapperFactory::value)
				.map(implCache::getInstance)
				.ifPresent(mapperFactory -> mapperFactories.add(0, mapperFactory));

		return new BindingSettings(forcedMapperFactory, mapperFactories, implCache);
	}
}
