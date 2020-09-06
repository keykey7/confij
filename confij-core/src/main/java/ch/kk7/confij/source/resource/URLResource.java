package ch.kk7.confij.source.resource;

import ch.kk7.confij.common.ServiceLoaderPriority;
import ch.kk7.confij.source.any.ConfijAnyResource;
import ch.kk7.confij.template.ValueResolver.StringResolver;
import com.google.auto.service.AutoService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;

@With
@Value
@NonFinal
@AllArgsConstructor
public class URLResource implements ConfijResource {
	@NonNull String urlTemplate;
	@NonNull String charsetTemplate;

	public static URLResource ofUrl(String urlTemplate) {
		return new URLResource(urlTemplate, ReadUtil.STANDARD_CHARSET.name());
	}

	public static URLResource ofUrl(URL url) {
		return ofUrl(url.toExternalForm());
	}

	public URLResource withCharset(Charset charset) {
		return withCharsetTemplate(charset.name());
	}

	@Override
	public Stream<String> read(StringResolver resolver) {
		String urlStr = resolver.resolve(urlTemplate);
		String charsetStr = resolver.resolve(charsetTemplate);
		return Stream.of(ReadUtil.readUrl(urlStr, charsetStr));
	}

	@ToString
	@AutoService(ConfijAnyResource.class)
	public static class AnyURLResource implements ConfijAnyResource, ServiceLoaderPriority {
		private static Optional<URL> maybeUrl(String maybeUrl) {
			try {
				return Optional.of(new URL(maybeUrl));
			} catch (MalformedURLException e) {
				return Optional.empty();
			}
		}

		@Override
		public int getPriority() {
			// lower prio, since it could handle file:// which we would like to be handled by a specialized service
			return ServiceLoaderPriority.DEFAULT_PRIORITY - 100;
		}

		@Override
		public Optional<URLResource> maybeHandle(String pathTemplate) {
			return maybeUrl(pathTemplate).map(URLResource::ofUrl);
		}
	}
}
