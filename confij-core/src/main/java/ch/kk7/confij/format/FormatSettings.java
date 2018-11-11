package ch.kk7.confij.format;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.annotation.VariableResolver;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.format.resolve.DefaultResolver;
import ch.kk7.confij.format.resolve.IVariableResolver;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FormatSettings {
	private final String defaultValue;
	private final Class<? extends IVariableResolver> variableResolverClass;
	private final LazyClassToImplCache implCache;

	protected FormatSettings(String defaultValue, Class<? extends IVariableResolver> variableResolverClass,
			LazyClassToImplCache implCache) {
		this.defaultValue = defaultValue;
		this.variableResolverClass = Objects.requireNonNull(variableResolverClass);
		this.implCache = Objects.requireNonNull(implCache);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public IVariableResolver getVariableResolver() {
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
		Optional<Class<? extends IVariableResolver>> variableResolverClassOpt = AnnotationUtil.findAnnotation(element,
				VariableResolver.class)
				.map(VariableResolver::value);
		Class<? extends IVariableResolver> variableResolverClass = variableResolverClassOpt.orElse(this.variableResolverClass);
		if (variableResolverClass == null) {
			throw new FormatException("An element annotated with {} has an invalid null resolver class", VariableResolver.class);
		}

		return new FormatSettings(defaultValue, variableResolverClass, implCache);
	}

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
					throw new Config4jException("unable to instantiate: " + k, e);
				}
			});
		}
	}
}
