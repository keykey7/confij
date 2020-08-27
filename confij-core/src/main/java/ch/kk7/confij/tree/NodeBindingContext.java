package ch.kk7.confij.tree;

import ch.kk7.confij.annotation.Default;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.common.ClassToImplCache;
import ch.kk7.confij.template.ValueResolver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.lang.reflect.AnnotatedElement;

/**
 * Immutable class to define (compile-time) definitions of how to map string configuration properties
 * to abstract nodes in a configuration context.
 * It's a binding-context since it can be modified using annotations.
 */
@With
@Value
public class NodeBindingContext {
	AnnotatedElement annotatedElement;

	String defaultValue;

	@NonNull ValueResolver valueResolver;

	@NonNull
	@With(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	ClassToImplCache implCache;

	public static NodeBindingContext newDefaultSettings(@NonNull ValueResolver valueResolver) {
		ClassToImplCache implCache = new ClassToImplCache();
		implCache.put(valueResolver);
		return new NodeBindingContext(null, null, valueResolver, implCache);
	}

	protected NodeBindingContext withValueResolverFor(AnnotatedElement element) {
		return withValueResolver(AnnotationUtil.findAnnotation(element, ch.kk7.confij.annotation.VariableResolver.class)
				.map(ch.kk7.confij.annotation.VariableResolver::value)
				.map(x -> implCache.getInstance(x, ValueResolver.class))
				.orElse(valueResolver));
	}

	protected NodeBindingContext withDefaultValueFor(AnnotatedElement element) {
		return withDefaultValue(AnnotationUtil.findAnnotation(element, Default.class)
				.map(x -> x.isNull() ? null : x.value())
				.orElse(defaultValue));
	}

	public NodeBindingContext settingsFor(AnnotatedElement element) {
		return withAnnotatedElement(element).withDefaultValueFor(element)
				.withValueResolverFor(element);
	}
}
