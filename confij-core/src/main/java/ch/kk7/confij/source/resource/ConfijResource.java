package ch.kk7.confij.source.resource;

import ch.kk7.confij.template.ValueResolver.StringResolver;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@FunctionalInterface
public interface ConfijResource {
	default Stream<ResourceContent> read() {
		return read(x -> x);
	}

	Stream<ResourceContent> read(StringResolver resolver);

	@UtilityClass
	class Defaults {
		final Charset CHARSET = StandardCharsets.UTF_8;
		final String CHARSET_NAME = CHARSET.name();
	}

	@Value
	@NonFinal
	class ResourceContent {
		String content;
		String path;
	}
}
