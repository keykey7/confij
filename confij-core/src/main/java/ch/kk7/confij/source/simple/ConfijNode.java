package ch.kk7.confij.source.simple;

import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.format.ConfigFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@ToString(onlyExplicitlyIncluded = true, doNotUseGetters = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConfijNode {
	@Getter
	@NonNull
	private final ConfigFormat config;
	@NonNull
	private final Map<String, ConfijNode> children = new LinkedHashMap<>();
	@NonNull
	private final ConfijNode root;
	@NonNull
	@ToString.Include
	@EqualsAndHashCode.Include
	private final URI uri;
	@ToString.Include
	private String value;

	protected ConfijNode(@NonNull ConfigFormat config) {
		this.config = config;
		this.root = this;
		this.uri = URI.create("config:/");
	}

	protected ConfijNode(ConfigFormat config, ConfijNode parent, String name) {
		this(config, parent.root, parent.uri.resolve(uriEncode(name) + (config.isValueHolder() ? "" : "/")));
	}

	protected ConfijNode(@NonNull ConfigFormat config, @NonNull ConfijNode root, @NonNull URI uri) {
		this.config = config;
		this.root = root;
		this.uri = uri;
	}

	protected static String uriEncode(String key) {
		try {
			return URLEncoder.encode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("cannot URL encode key: " + key, e);
		}
	}

	public static ConfijNode newRootFor(ConfigFormat configFormat) {
		return new ConfijNode(configFormat);
	}

	public ConfijNode deepClone() {
		if (!isRootNode()) {
			throw new IllegalArgumentException("cannot clone a non-root node");
		}
		return deepClone(null);
	}

	protected ConfijNode deepClone(ConfijNode rootNode) {
		final ConfijNode clone;
		if (rootNode == null) {
			clone = new ConfijNode(config);
		} else {
			clone = new ConfijNode(config, rootNode, this.uri);
		}
		children.forEach((k, v) -> clone.children.put(k, v.deepClone(rootNode)));
		clone.value = value;
		return clone;
	}

	public String resolveValue(String targetUriStr) {
		return resolve(URI.create(uriEncode(targetUriStr))).getValue();
	}

	@NonNull
	public ConfijNode resolve(String targetStr) {
		return resolve(uri.resolve(uriEncode(targetStr)));
	}

	@NonNull
	public ConfijNode resolve(URI target) {
		URI relativeTarget = uri.relativize(target);
		if (relativeTarget.equals(target)) {
			// only happens when called initially with an absolute URI, which is in another subtree than this node
			String rootScheme = root.uri.getScheme();
			String targetScheme = relativeTarget.getScheme();
			if (!rootScheme.equals(targetScheme)) {
				throw new SimpleConfigException("unknown scheme '{}', expected is '{}'", targetScheme, rootScheme);
			}
			return root.resolve(target);
		}
		String targetPath = relativeTarget.getPath();
		if ("".equals(targetPath)) {
			return this;
		}
		String firstPart = targetPath.split("/", 2)[0];
		ConfijNode child = children.get(firstPart);
		if (child == null) {
			throw new SimpleConfigException("invalid path {}: node {} doesn't have a child named '{}'", target, uri, firstPart);
		}
		return child.resolve(target);
	}

	@ToString.Include
	protected boolean isRootNode() {
		return root == this;
	}

	@NonNull
	public ConfijNode addChild(String key) {
		assertBranchNode();
		if (children.containsKey(key)) {
			throw new IllegalStateException("node " + this + " already contains a child named '" + key + "'");
		}
		ConfigFormat childConfig = config.formatForChild(key);
		ConfijNode child = new ConfijNode(childConfig, this, key);
		children.put(key, child);
		return child;
	}

	@NonNull
	public Map<String, ConfijNode> getChildren() {
		return Collections.unmodifiableMap(children);
	}

	public String getValue() {
		assertLeafNode();
		return value;
	}

	public void setValue(String value) {
		assertLeafNode();
		this.value = value;
	}

	private void assertLeafNode() {
		if (!config.isValueHolder()) {
			throw new SimpleConfigException("expected a leaf-node, but got {}", this);
		}
	}

	private void assertBranchNode() {
		if (config.isValueHolder()) {
			throw new SimpleConfigException("expected a branch-node but got {}", this);
		}
	}

	public ConfijNode initializeFromMap(Object config) {
		if (config instanceof String || config == null) {
			setValue((String) config);
		} else if (config instanceof List) {
			ListIterator<?> iterator = ((List<?>) config).listIterator();
			while (iterator.hasNext()) {
				int index = iterator.nextIndex();
				String indexStr = String.valueOf(index);
				addChild(indexStr).initializeFromMap(iterator.next());
			}
		} else if (config instanceof Map) {
			//noinspection unchecked
			((Map<String, ?>) config).forEach((k, v) -> addChild(k).initializeFromMap(v));
		} else {
			throw new Config4jException("initializeFromMap for unknown type " + config);
		}
		return this;
	}

	// TODO: worth an idea: maybe we should clone when needed in here and make this class immutable
	public void overrideWith(ConfijNode other) {
		if (config != other.config) {
			throw new IllegalArgumentException("cannot override with non-equal node");
		}
		value = other.value;
		other.children.forEach((otherKey, otherChild) -> {
			final ConfijNode child;
			if (children.containsKey(otherKey)) {
				child = children.get(otherKey);
			} else {
				child = addChild(otherKey);
			}
			child.overrideWith(otherChild);
		});
	}
}
