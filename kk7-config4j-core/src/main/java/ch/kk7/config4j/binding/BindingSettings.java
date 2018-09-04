package ch.kk7.config4j.binding;

import ch.kk7.config4j.annotation.ValueMapper;
import ch.kk7.config4j.binding.leaf.IValueMapper;
import ch.kk7.config4j.format.FormatSettings.LazyClassToImplCache;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Optional;

import static ch.kk7.config4j.common.Util.getSoloAnnotationsByType;

public class BindingSettings {
	private final Class<? extends IValueMapper> valueMapperClass;
	private final LazyClassToImplCache implCache;

	protected BindingSettings(Class<? extends IValueMapper> valueMapperClass, LazyClassToImplCache implCache) {
		this.valueMapperClass = valueMapperClass;
		this.implCache = Objects.requireNonNull(implCache);
	}

	public static BindingSettings newDefaultSettings() {
		return new BindingSettings(null, new LazyClassToImplCache());
	}

	public Optional<IValueMapper> getValueMapper() {
		return Optional.ofNullable(valueMapperClass)
				.map(implCache::getInstance);
	}

	public BindingSettings settingsFor(AnnotatedElement element) {
		// handle value mapping of the next type
		Optional<Class<? extends IValueMapper>> valueMapperClassOpt = getSoloAnnotationsByType(element, ValueMapper.class).map(
				ValueMapper::value);
		final Class<? extends IValueMapper> valueMapperClass = valueMapperClassOpt.orElse(this.valueMapperClass);
		return new BindingSettings(valueMapperClass, implCache);
	}
}
