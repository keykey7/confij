package ch.kk7.confij.source.logical;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfijSource;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
public class OrSource implements ConfijSource {
	private static final ConfijLogger LOG = ConfijLogger.getLogger(OrSource.class.getName());
	private final List<ConfijSource> orList;

	public OrSource(@NonNull ConfijSource one, @NonNull ConfijSource or, ConfijSource... orEven) {
		orList = new ArrayList<>();
		orList.add(one);
		orList.add(or);
		orList.addAll(Arrays.asList(orEven));
	}

	@Override
	public void override(ConfijNode rootNode) {
		List<Exception> pastExceptions = new ArrayList<>();
		for (ConfijSource source : orList) {
			ConfijNode copy = rootNode.deepClone();
			try {
				source.override(copy);
			} catch (Exception e) {
				pastExceptions.add(e);
				LOG.info("failed reading optional source {}", source, e);
				continue;
			}
			rootNode.overrideWith(copy);
			return;
		}
		ConfijSourceException e = new ConfijSourceException("failed to read any of the sources: {}", this);
		pastExceptions.forEach(e::addSuppressed);
		throw e;
	}
}
