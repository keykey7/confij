package ch.kk7.confij.source.logical;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.tree.ConfijNode;
import lombok.NonNull;
import lombok.Value;

import java.util.logging.Level;
import java.util.logging.Logger;

@Value
public class MaybeSource implements ConfigSource {
	@NonNull
	private final ConfigSource maybeSource;

	@Override
	public void override(ConfijNode rootNode) {
		ConfijNode copy = rootNode.deepClone();
		try {
			// simulate it first
			maybeSource.override(copy);
		} catch (Exception e) {
			// poor mans logging, but cannot just drop it either
			Logger.getLogger(MaybeSource.class.getName()).log(Level.INFO, "failed reading optional source " + maybeSource, e);
		}
		rootNode.overrideWith(copy);
	}
}
