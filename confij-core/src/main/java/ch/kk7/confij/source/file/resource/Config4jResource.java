package ch.kk7.confij.source.file.resource;

import java.net.URI;

public interface Config4jResource {

	String read(URI path);

	boolean canHandle(URI path);
}
