package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.Util;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.template.ValueResolver.StringResolver;
import com.google.auto.service.AutoService;
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
	@With
	Class<?> classloaderOf;

	public ClasspathResource withCharset(Charset charset) {
		return withCharsetTemplate(charset.name());
	}

	public static ClasspathResource ofName(String nameTemplate) {
		return new ClasspathResource(nameTemplate, Defaults.CHARSET_NAME, null);
	}

	@NonNull
	protected URL asUrl(String name) {
		final URL url;
		if (classloaderOf == null) {
			url = ClassLoader.getSystemResource(name);
		} else {
			url = classloaderOf.getResource(name);
		}
		if (url == null) {
			throw unableToFetch(name, "no such file on classpath");
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
