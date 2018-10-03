package ch.kk7.confij.source.logical;

import ch.kk7.confij.source.Config4jSourceException;
import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.simple.SimpleConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrSource implements ConfigSource {
	private final List<ConfigSource> orList;

	public OrSource(ConfigSource... or) {
		if (or.length < 2) {
			throw new IllegalArgumentException("missing args in OrSource");
		}
		this.orList = Arrays.asList(or);
	}
	@Override
	public void override(SimpleConfig simpleConfig) {
		List<Exception> pastExceptions = new ArrayList<>();
		for (ConfigSource source : orList) {
			SimpleConfig copy = simpleConfig.copy();
			try {
				source.override(copy);
			} catch (Exception e) {
				pastExceptions.add(e);
				continue;
			}
			simpleConfig.overrideWith(copy);
			return;
		}
		Config4jSourceException e = new Config4jSourceException("failed to read any of the sources: {}", this);
		pastExceptions.forEach(e::addSuppressed);
		throw e;
	}

	@Override
	public String toString() {
		return "Or{" + orList + '}';
	}
}
