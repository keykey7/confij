package ch.kk7.confij.source.resource;

import ch.kk7.confij.template.ValueResolver.StringResolver;

import java.util.stream.Stream;

@FunctionalInterface
public interface ConfijResource {
	Stream<String> read(StringResolver resolver);
}
