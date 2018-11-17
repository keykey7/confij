package ch.kk7.confij.source.file.resource;

import java.net.URI;

public interface ConfijResourceProvider {

	String read(URI path);

	boolean canHandle(URI path);
}
