package ch.kk7.config4j.source.simple;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static ch.kk7.config4j.common.Util.assertClass;

public abstract class SimpleConfig {
	private final ConfigFormat config;
	private final SimpleConfig parent;
	private final SimpleConfig root;
	private final URI uri;

	protected SimpleConfig(ConfigFormat config) {
		this.config = Objects.requireNonNull(config);
		this.parent = null;
		this.root = this;
		this.uri = URI.create("config:/");
		assertFormatMatches();
	}

	protected SimpleConfig(SimpleConfig parent, String key) {
		this.parent = Objects.requireNonNull(parent);
		this.root = parent.root;
		if (Objects.requireNonNull(key)
				.isEmpty()) {
			throw new IllegalArgumentException("empty key is not allowed");
		}
		String uriTerminator = this instanceof SimpleConfigLeaf ? "" : "/";
		this.uri = parent.uri.resolve(key + uriTerminator);
		if (parent.config instanceof ConfigFormatList) {
			config = ((ConfigFormatList) parent.config).anyChild();
		} else if (parent.config instanceof ConfigFormatMap) {
			config = ((ConfigFormatMap) parent.config).get(key)
					.orElseThrow(() -> new IllegalStateException("not an anyMap"));
		} else {
			throw new IllegalStateException("parent is neither list nor map: " + parent.config);
		}
		assertFormatMatches();
	}

	private void assertFormatMatches() {
		if (this instanceof SimpleConfigLeaf) {
			assertClass(config, ConfigFormatLeaf.class);
		} else if (this instanceof SimpleConfigList) {
			assertClass(config, ConfigFormatList.class);
		} else if (this instanceof SimpleConfigMap) {
			assertClass(config, ConfigFormatMap.class);
		} else {
			throw new IllegalStateException("unknown self");
		}
	}

	public SimpleConfig copy() {
		// TODO: clone here and restore in a reasonable manner
		return SimpleConfig.fromObject(toObject(), getConfig());
	}

	public ConfigFormat getConfig() {
		return config;
	}

	public URI getUri() {
		return uri;
	}

	public SimpleConfigLeaf resolveLeaf(String s) {
		SimpleConfig config = resolve(s);
		if (config instanceof SimpleConfigLeaf) {
			return (SimpleConfigLeaf) config;
		}
		throw new Config4jException("unable to resolveString leaf value: path does not denote a leaf");
	}

	public SimpleConfig resolve(String s) {
		URI targetUri = this.uri.resolve(s)
				.normalize();
		if (!root.uri.getScheme()
				.equals(targetUri.getScheme())) {
			throw new IllegalArgumentException("not a valid config scheme: " + targetUri.getScheme());
		}
		String path = targetUri.getSchemeSpecificPart();
		String[] parts = path.split("/", -1);
		if (parts.length < 2) {
			throw new IllegalStateException("unexpected SchemeSpecificPart not strarting with '/': " + path);
		}
		String[] partsWithoutFirst = Arrays.copyOfRange(parts, 1, parts.length);
		return root.resolve(partsWithoutFirst);
	}

	public abstract SimpleConfig resolve(String... path);

	public abstract Object toObject();

	public abstract void overrideWith(SimpleConfig simpleConfig);

	public static SimpleConfig newRootFor(ConfigFormat configFormat) {
		if (configFormat instanceof ConfigFormatLeaf) {
			return new SimpleConfigLeaf(configFormat);
		} else if (configFormat instanceof ConfigFormatList) {
			return new SimpleConfigList(configFormat);
		} else if (configFormat instanceof ConfigFormatMap) {
			return new SimpleConfigMap(configFormat);
		} else {
			throw new IllegalStateException("unknown ConfigFormat: " + configFormat);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleConfig fromObject(Object config, ConfigFormat configFormat) {
		if (config instanceof String || config == null) {
			SimpleConfigLeaf root = new SimpleConfigLeaf(configFormat);
			root.set((String) config);
			return root;
		}
		if (config instanceof List) {
			SimpleConfigList root = new SimpleConfigList(configFormat);
			root.fromObject((List<?>) config);
			return root;
		}
		if (config instanceof Map) {
			SimpleConfigMap root = new SimpleConfigMap(configFormat);
			root.fromObject((Map<?, ?>) config);
			return root;
		}
		throw new Config4jException("root configuration is of unknown type " + config);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SimpleConfig)) {
			return false;
		}
		SimpleConfig that = (SimpleConfig) o;
		if (!uri.equals(that.uri)) {
			return false;
		}
		return Objects.equals(hashMe(), that.hashMe());
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri, hashMe());
	}

	protected abstract Object hashMe();

	public abstract Stream<SimpleConfigLeaf> leaves();

	public SimpleConfigLeaf asLeaf() {
		return null;
	}

	public SimpleConfigList asList() {
		return null;
	}

	public SimpleConfigMap asMap() {
		return null;
	}
}
