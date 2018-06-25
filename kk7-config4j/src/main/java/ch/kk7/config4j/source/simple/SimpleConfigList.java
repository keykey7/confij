package ch.kk7.config4j.source.simple;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatLeaf;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.kk7.config4j.common.Util.assertClass;

public class SimpleConfigList extends SimpleConfig {
	private final List<SimpleConfig> list = new ArrayList<>();

	public SimpleConfigList(ConfigFormat config) {
		super(config);
	}

	protected SimpleConfigList(SimpleConfig parent, String key) {
		super(parent, key);
	}

	@Override
	public ch.kk7.config4j.source.simple.SimpleConfigList asList() {
		return this;
	}

	@Override
	public SimpleConfig resolve(String... path) {
		if (Objects.requireNonNull(path).length == 0) {
			return this;
		}
		if (list.isEmpty()) {
			throw SimpleConfigException.newResolvePathException(getUri(), Arrays.asList(path), "none (list is empty)");
		}
		final int index;
		try {
			index = Integer.valueOf(path[0]);
		} catch (NumberFormatException e) {
			int max = list.size() - 1;
			throw SimpleConfigException.newResolvePathException(getUri(), path[0], "numbers from 0 to " + max);
		}
		if (index < 0 || index >= list.size()) {
			int max = list.size() - 1;
			throw SimpleConfigException.newResolvePathException(getUri(), index, "numbers from 0 to " + max);
		}
		String[] subPath = Arrays.copyOfRange(path, 1, path.length);
		return list.get(index)
				.resolve(subPath);
	}

	public List<SimpleConfig> list() {
		return list;
	}

	public SimpleConfig add() {
		ConfigFormat configFormat = getConfig();
		if (configFormat instanceof ConfigFormatLeaf) {
			return addLeaf();
		} else if (configFormat instanceof ConfigFormatList) {
			return addList();
		} else if (configFormat instanceof ConfigFormatMap) {
			return addMap();
		} else {
			throw new IllegalStateException("unknown ConfigFormat: " + configFormat);
		}
	}

	public SimpleConfigList addList() {
		SimpleConfigList node = new SimpleConfigList(this, String.valueOf(list.size()));
		list.add(node);
		return node;
	}

	public SimpleConfigMap addMap() {
		SimpleConfigMap node = new SimpleConfigMap(this, String.valueOf(list.size()));
		list.add(node);
		return node;
	}

	public SimpleConfigLeaf addLeaf() {
		SimpleConfigLeaf node = new SimpleConfigLeaf(this, String.valueOf(list.size()));
		list.add(node);
		return node;
	}

	public void fromObject(List<?> config) {
		config.forEach(v -> {
			if (v instanceof String) {
				addLeaf().set((String) v);
			} else if (v instanceof List) {
				addList().fromObject((List<?>) v);
			} else if (v instanceof Map) {
				addMap().fromObject((Map<?, ?>) v);
			} else {
				throw new Config4jException("list contains unknown file " + v + " in " + config);
			}
		});
	}

	@Override
	public List<Object> toObject() {
		return Collections.unmodifiableList(list.stream()
				.map(SimpleConfig::toObject)
				.collect(Collectors.toList()));
	}

	@Override
	public void overrideWith(SimpleConfig simpleConfig) {
		ch.kk7.config4j.source.simple.SimpleConfigList other = assertClass(simpleConfig,
				ch.kk7.config4j.source.simple.SimpleConfigList.class);
		for (int i = 0; i < other.list.size(); i++) {
			SimpleConfig otherItem = other.list.get(i);
			SimpleConfig currentItem = list.size() >= i ? null : list.get(i);
			if (currentItem == null) {
				list.set(i, otherItem);
			} else {
				currentItem.overrideWith(otherItem);
			}
		}
	}

	@Override
	protected Object hashMe() {
		return list;
	}

	@Override
	public Stream<SimpleConfigLeaf> leaves() {
		return list.stream()
				.flatMap(SimpleConfig::leaves);
	}
}
