package ch.kk7.confij.source.file.format;

import ch.kk7.confij.tree.ConfijNode;

import java.net.URI;
import java.util.Optional;

public interface ConfijSourceFormat {
	void override(ConfijNode confijNode, String configAsStr);

	boolean canHandle(URI path);

	static boolean canHandleIfMatches(URI path, String regex) {
		return Optional.ofNullable(path.getFragment())
				.filter(x -> x.matches(regex))
				.isPresent() ||
				path.getSchemeSpecificPart()
						.matches(regex);
	}
}
