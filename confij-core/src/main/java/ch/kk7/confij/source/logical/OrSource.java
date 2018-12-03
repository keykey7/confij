package ch.kk7.confij.source.logical;

import ch.kk7.confij.logging.ConfijLogger;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.ConfijSourceException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
public class OrSource implements ConfigSource {
	private static final ConfijLogger LOG = ConfijLogger.getLogger(OrSource.class.getName());
	private final List<ConfigSource> orList;

	public OrSource(@NonNull ConfigSource one, @NonNull ConfigSource or, ConfigSource... orEven) {
		orList = new ArrayList<>();
		orList.add(one);
		orList.add(or);
		orList.addAll(Arrays.asList(orEven));
	}

	@Override
	public void override(ConfijNode rootNode) {
		List<Exception> pastExceptions = new ArrayList<>();
		for (ConfigSource source : orList) {
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
