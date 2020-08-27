package ch.kk7.confij.source.logical;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class MaybeSource implements ConfijSource {
	private static final ConfijLogger LOG = ConfijLogger.getLogger(MaybeSource.class.getName());

	@NonNull
	private final ConfijSource source;

	@Override
	public void override(ConfijNode rootNode) {
		ConfijNode copy = rootNode.deepClone();
		try {
			// simulate it first
			source.override(copy);
		} catch (Exception e) {
			LOG.info("failed reading optional source {}", this, e);
			return;
		}
		rootNode.overrideWith(copy);
	}
}
