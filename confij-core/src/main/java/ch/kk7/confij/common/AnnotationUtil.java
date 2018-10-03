package ch.kk7.confij.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * taken from:
 * https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/annotation/AnnotationUtils.html
 */
public class AnnotationUtil {
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
		// TODO: cache the result
		return Optional.ofNullable(findAnnotation(annotatedElement, annotationType, new HashSet<>()));
	}

	private static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType,
			Set<Annotation> visited) {
		A annotation = annotatedElement.getDeclaredAnnotation(annotationType);
		if (annotation != null) {
			return annotation;
		}
		for (Annotation declaredAnn : annotatedElement.getDeclaredAnnotations()) {
			Class<? extends Annotation> declaredType = declaredAnn.annotationType();
			if (!isInJavaLangAnnotationPackage(declaredType) && visited.add(declaredAnn)) {
				annotation = findAnnotation(declaredType, annotationType, visited);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		return null;
	}

	static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
		return (annotationType != null && isInJavaLangAnnotationPackage(annotationType.getName()));
	}

	public static boolean isInJavaLangAnnotationPackage(String annotationType) {
		return (annotationType != null && annotationType.startsWith("java.lang.annotation"));
	}
}
