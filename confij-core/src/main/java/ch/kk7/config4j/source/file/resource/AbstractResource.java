package ch.kk7.config4j.source.file.resource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class AbstractResource implements Config4jResource {
	private Charset charset = StandardCharsets.UTF_8;

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = Objects.requireNonNull(charset, "null charset");
	}
}
