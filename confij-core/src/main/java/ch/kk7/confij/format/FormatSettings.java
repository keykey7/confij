package ch.kk7.confij.format;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.format.resolve.DefaultResolver;
import ch.kk7.confij.format.resolve.VariableResolver;
import lombok.ToString;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ToString
public class FormatSettings {
	private final String defaultValue;
	private final Class<? extends VariableResolver> variableResolverClass;
	private final LazyClassToImplCache implCache;

	protected FormatSettings(String defaultValue, Class<? extends VariableResolver> variableResolverClass,
			LazyClassToImplCache implCache) {
		this.defaultValue = defaultValue;
		this.variableResolverClass = Objects.requireNonNull(variableResolverClass);
		this.implCache = Objects.requireNonNull(implCache);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public VariableResolver getVariableResolver() {
		return implCache.getInstance(variableResolverClass);
	}

	public static FormatSettings newDefaultSettings() {
		return new FormatSettings( null, DefaultResolver.class, new LazyClassToImplCache());
	}

	public FormatSettings settingsFor(AnnotatedElement element) {
		// handle default config values
		String defaultValue = AnnotationUtil.findAnnotation(element, Default.class)
				.map(Default::value)
				.orElse(null); // not inheriting the old default value on purpose here

		// handle variable resolver
		Optional<Class<? extends VariableResolver>> variableResolverClassOpt = AnnotationUtil.findAnnotation(element,
				ch.kk7.confij.annotation.VariableResolver.class)
				.map(ch.kk7.confij.annotation.VariableResolver::value);
		Class<? extends VariableResolver> variableResolverClass = variableResolverClassOpt.orElse(this.variableResolverClass);
		if (variableResolverClass == null) {
			throw new FormatException("An element annotated with {} has an invalid null resolver class", ch.kk7.confij.annotation.VariableResolver.class);
		}

		return new FormatSettings(defaultValue, variableResolverClass, implCache);
	}

	@ToString
	public static class LazyClassToImplCache {
		private final Map<Class<?>, Object> instances = new HashMap<>();

		@SuppressWarnings("unchecked")
		public <T> T getInstance(Class<T> clazz) {
			return (T) instances.computeIfAbsent(clazz, k -> {
				try {
					Constructor<?> constructor = k.getDeclaredConstructor();
					if (!constructor.isAccessible()) {
						constructor.setAccessible(true);
					}
					return constructor.newInstance();
				} catch (Exception e) {
					throw new ConfijException("unable to instantiate: " + k, e);
				}
			});
		}

		public <T> T getInstance(Class<? extends T> clazz, Class<T> asClass) {
			return asClass.cast(getInstance(clazz));
		}
	}
}
