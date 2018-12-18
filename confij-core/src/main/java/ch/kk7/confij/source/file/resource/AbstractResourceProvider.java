package ch.kk7.confij.source.file.resource;

import lombok.Data;
import lombok.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data
public abstract class AbstractResourceProvider implements ConfijResourceProvider {
	@NonNull
	private Charset charset = StandardCharsets.UTF_8;
}
