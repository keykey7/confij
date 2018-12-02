package ch.kk7.confij.tree;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.ClassToImplCache;
import ch.kk7.confij.template.DefaultResolver;
import ch.kk7.confij.template.VariableResolver;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Wither;

import java.lang.reflect.AnnotatedElement;

/**
 * Immutable class to define (compile-time) definitions of how to map string configuration properties
 * to abstract nodes in a configuration context.
 * It's a binding-context since it can be modified using annotations.
 */
@Wither
@ToString
@AllArgsConstructor
public class NodeBindingContext {
	@Getter
	private final String defaultValue;
	@NonNull
	@Getter
	private final VariableResolver variableResolver;
	@NonNull
	@Wither(AccessLevel.NONE)
	private final ClassToImplCache implCache;

	public static NodeBindingContext newDefaultSettings() {
		ClassToImplCache implCache = new ClassToImplCache();
		return new NodeBindingContext( null, implCache.getInstance(DefaultResolver.class), implCache);
	}

	protected NodeBindingContext withVariableResolverFor(AnnotatedElement element) {
		return withVariableResolver(AnnotationUtil.findAnnotation(element, ch.kk7.confij.annotation.VariableResolver.class)
				.map(ch.kk7.confij.annotation.VariableResolver::value)
				.map(x -> implCache.getInstance(x, VariableResolver.class))
				.orElse(variableResolver));
	}

	protected NodeBindingContext withDefaultValueFor(AnnotatedElement element) {
		return withDefaultValue(AnnotationUtil.findAnnotation(element, Default.class)
				.map(Default::value)
				.orElse(defaultValue));
	}

	public NodeBindingContext settingsFor(AnnotatedElement element) {
		return withDefaultValueFor(element).withVariableResolverFor(element);
	}
}
