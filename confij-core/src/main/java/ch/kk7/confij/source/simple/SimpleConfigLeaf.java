package ch.kk7.confij.source.simple;

import ch.kk7.confij.format.ConfigFormat;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static ch.kk7.confij.common.Util.assertClass;

public class SimpleConfigLeaf extends SimpleConfig {
	private String value;

	public SimpleConfigLeaf(ConfigFormat config) {
		super(config);
	}

	protected SimpleConfigLeaf(SimpleConfig parent, String key) {
		super(parent, key);
	}

	@Override
	public ch.kk7.confij.source.simple.SimpleConfigLeaf asLeaf() {
		return this;
	}

	@Override
	public SimpleConfig resolve(String... path) {
		if (Objects.requireNonNull(path).length != 0) {
			throw SimpleConfigException.newResolvePathException(getUri(), Arrays.asList(path), "none (it's a leaf)");
		}
		return this;
	}

	public String get() {
		return value;
	}

	public ch.kk7.confij.source.simple.SimpleConfigLeaf set(String value) {
		this.value = value;
		return this;
	}

	@Override
	public Object toObject() {
		return value;
	}

	@Override
	public void overrideWith(SimpleConfig simpleConfig) {
		set(assertClass(simpleConfig, ch.kk7.confij.source.simple.SimpleConfigLeaf.class).get());
	}

	@Override
	protected Object hashMe() {
		return value;
	}

	@Override
	public Stream<SimpleConfigLeaf> leaves() {
		return Stream.of(this);
	}
}
