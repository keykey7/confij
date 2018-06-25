package ch.kk7.config4j.format;

import ch.kk7.config4j.annotation.Default;
import ch.kk7.config4j.annotation.NotNull;
import ch.kk7.config4j.annotation.Nullable;
import ch.kk7.config4j.annotation.VariableResolver;
import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.format.resolve.DefaultResolver;
import ch.kk7.config4j.format.resolve.IVariableResolver;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import static ch.kk7.config4j.common.Util.getSoloAnnotationsByType;

public class FormatSettings {
	private final boolean isNullAllowed;
	private final String defaultValue;
	private final Class<? extends IVariableResolver> variableResolverClass;
	private final Map<Class<? extends IVariableResolver>, IVariableResolver> resolverInstances;

	protected FormatSettings(boolean isNullAllowed, String defaultValue, Class<? extends IVariableResolver> variableResolverClass,
			Map<Class<? extends IVariableResolver>, IVariableResolver> resolverInstances) {
		this.isNullAllowed = isNullAllowed;
		this.defaultValue = defaultValue;
		this.variableResolverClass = variableResolverClass;
		this.resolverInstances = resolverInstances;
	}

	public boolean isNullAllowed() {
		return isNullAllowed;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public IVariableResolver getVariableResolver() {
		return resolverInstances.computeIfAbsent(variableResolverClass, k -> {
			try {
				return k.getConstructor()
						.newInstance();
			} catch (Exception e) {
				throw new Config4jException("unable to instantiate: " + k, e);
			}
		});
	}

	public static FormatSettings newDefaultSettings() {
		return new FormatSettings(false, null, DefaultResolver.class, new HashMap<>());
	}

	public FormatSettings settingsFor(AnnotatedElement element) {
		// handle null config values
		boolean willNullBeAllowed = isNullAllowed;
		boolean forceNullable = element.isAnnotationPresent(Nullable.class);
		boolean forceNotNull = element.isAnnotationPresent(NotNull.class);
		if (forceNullable && forceNotNull) {
			throw new IllegalArgumentException(Nullable.class + " and " + NotNull.class + " cannot be present at once");
		}
		if (forceNullable) {
			willNullBeAllowed = true;
		}
		if (forceNotNull) {
			willNullBeAllowed = false;
		}

		// handle default config values
		String defaultValue = getSoloAnnotationsByType(element, Default.class).map(Default::value)
				.orElse(null); // not inheriting the old default value on purpose here

		// handle variable resolver
		Class<? extends IVariableResolver> vrClass = getSoloAnnotationsByType(element, VariableResolver.class).map(VariableResolver::value)
				.orElse(null);
		if (vrClass == null) {
			vrClass = variableResolverClass;
		}

		return new FormatSettings(willNullBeAllowed, defaultValue, vrClass, resolverInstances);
	}
}
