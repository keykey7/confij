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

@ToString
@AllArgsConstructor
@Wither
public class FormatSettings {
	@Getter
	private final String defaultValue;
	@NonNull
	@Getter
	private final VariableResolver variableResolver;
	@NonNull
	@Wither(AccessLevel.NONE)
	private final LazyClassToImplCache implCache;

	public static FormatSettings newDefaultSettings() {
		LazyClassToImplCache implCache = new LazyClassToImplCache();
		return new FormatSettings( null, implCache.getInstance(DefaultResolver.class), implCache);
	}

	protected FormatSettings withVariableResolverFor(AnnotatedElement element) {
		return withVariableResolver(AnnotationUtil.findAnnotation(element, ch.kk7.confij.annotation.VariableResolver.class)
				.map(ch.kk7.confij.annotation.VariableResolver::value)
				.map(x -> implCache.getInstance(x, VariableResolver.class))
				.orElse(variableResolver));
	}

	protected FormatSettings withDefaultValueFor(AnnotatedElement element) {
		return withDefaultValue(AnnotationUtil.findAnnotation(element, Default.class)
				.map(Default::value)
				.orElse(defaultValue));
	}

	public FormatSettings settingsFor(AnnotatedElement element) {
		return withDefaultValueFor(element).withVariableResolverFor(element);
	}
}
