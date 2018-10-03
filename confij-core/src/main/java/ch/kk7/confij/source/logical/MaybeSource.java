package ch.kk7.confij.source.logical;

import ch.kk7.confij.source.ConfigSource;
import ch.kk7.confij.source.simple.SimpleConfig;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MaybeSource implements ConfigSource {
	private final ConfigSource maybeSource;

	public MaybeSource(ConfigSource maybeSource) {
		this.maybeSource = Objects.requireNonNull(maybeSource, "null maybeSource");
	}

	@Override
	public void override(SimpleConfig simpleConfig) {
		SimpleConfig copy = simpleConfig.copy();
		try {
			// simulate it first
			maybeSource.override(copy);
		} catch (Exception e) {
			// poor mans logging, but cannot just drop it either
			Logger.getLogger(MaybeSource.class.getName()).log(Level.INFO, "failed reading optional source " + maybeSource, e);
		}
		simpleConfig.overrideWith(copy);
	}

	@Override
	public String toString() {
		return "Maybe{" + maybeSource + '}';
	}
}
