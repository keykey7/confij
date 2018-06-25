package ch.kk7.config4j.source.file.resource;

@FunctionalInterface
public interface Config4jResource {

	String read(String path);
}
