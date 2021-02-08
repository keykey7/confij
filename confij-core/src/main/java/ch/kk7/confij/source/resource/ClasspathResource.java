package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.template.ValueResolver.StringResolver;
import com.google.auto.service.AutoService;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.kk7.confij.source.resource.ConfijSourceFetchingException.unableToFetch;

@Value
@NonFinal
public class ClasspathResource implements ConfijResource {
	@NonNull String nameTemplate;
	@With
	@NonNull String charsetTemplate;
	@With(AccessLevel.PROTECTED)
	Class<?> relativeClass;
	@With(AccessLevel.PROTECTED)
	ClassLoader relativeClassLoader;

	public ClasspathResource relativeTo(ClassLoader relativeClassLoader) {
		return withRelativeClassLoader(relativeClassLoader);
	}

	public ClasspathResource relativeTo(Class<?> relativeClass) {
		return withRelativeClass(relativeClass);
	}

	/**
	 * @deprecated use {@link #relativeTo(Class)} instead
	 */
	@Deprecated
	public ClasspathResource withClassloaderOf(Class<?> relativeClass) {
		return relativeTo(relativeClass);
	}

	public ClasspathResource withCharset(Charset charset) {
		return withCharsetTemplate(charset.name());
	}

	public static ClasspathResource ofName(String nameTemplate) {
		return new ClasspathResource(nameTemplate, Defaults.CHARSET_NAME, null, Thread.currentThread()
				.getContextClassLoader());
	}

	@NonNull
	protected URL asUrl(String name) {
		final URL url;
		if (relativeClass != null) {
			url = relativeClass.getResource(name);
			if (url == null) {
				throw unableToFetch(name, "no such file on classpath relative to class " + relativeClass.getName());
			}
			return url;
		}
		url = relativeClassLoader.getResource(name);
		if (url == null) {
			throw unableToFetch(name, "no such file on classpath of classloader"  + relativeClassLoader);
		}
		return url;
	}

	@Override
	public Stream<ResourceContent> read(StringResolver resolver) {
		String name = resolver.resolve(nameTemplate);
		Charset charset = Charset.forName(resolver.resolve(charsetTemplate));
		URL classpathUrl = asUrl(name);
		return Stream.of(new ResourceContent(URLResource.readUrl(classpathUrl, charset), name));
	}

	@ToString
	@AutoService(ConfijAnyResource.class)
	public static class AnyClasspathResource implements ConfijAnyResource {
		public static final String SCHEME = "classpath";

		@Override
		public Optional<ClasspathResource> maybeHandle(String pathTemplate) {
			return Util.getScheme(pathTemplate)
					.filter(scheme -> scheme.equals(SCHEME))
					.map(scheme -> {
						String path = Util.getSchemeSpecificPart(pathTemplate);
						return ClasspathResource.ofName(path);
					});
		}
	}
}
