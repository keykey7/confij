package ch.kk7.confij.format;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.LazyClassToImplCache;
import ch.kk7.confij.format.resolve.DefaultResolver;
import ch.kk7.confij.format.resolve.VariableResolver;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Wither;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@ToString
@AllArgsConstructor
@Wither(AccessLevel.PROTECTED)
public class FormatSettings {
	@Getter
	private final String defaultValue;
	@NonNull
	private final Class<? extends VariableResolver> variableResolverClass;
	@NonNull
	@Wither(AccessLevel.NONE)
	private final LazyClassToImplCache implCache;

	public VariableResolver getVariableResolver() {
		return implCache.getInstance(variableResolverClass);
	}

	public static FormatSettings newDefaultSettings() {
		return new FormatSettings( null, DefaultResolver.class, new LazyClassToImplCache());
	}

	protected FormatSettings withVariableResolverFor(AnnotatedElement element) {
		Optional<Class<? extends VariableResolver>> variableResolverClassOpt = AnnotationUtil.findAnnotation(element,
				ch.kk7.confij.annotation.VariableResolver.class)
				.map(ch.kk7.confij.annotation.VariableResolver::value);
		return withVariableResolver(variableResolverClassOpt.orElse(this.variableResolverClass));
	}

	public <T extends VariableResolver> FormatSettings withVariableResolver(Class<T> variableResolverClass) {
		return withVariableResolverClass(variableResolverClass);
	}

	protected FormatSettings withDefaultValueFor(AnnotatedElement element) {
		// not inheriting the old default value on purpose here
		return withDefaultValue(AnnotationUtil.findAnnotation(element, Default.class)
				.map(Default::value)
				.orElse(null));
	}

	public FormatSettings settingsFor(AnnotatedElement element) {
		return withDefaultValueFor(element).withVariableResolverFor(element);
	}
}
