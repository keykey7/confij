package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.experimental.UtilityClass;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class CollectionUtil {
	/**
	 * @deprecated unused, favour {@link #childrenAsContinuousList(ConfijNode)}
	 */
	@Deprecated
	public static Map<Integer, ConfijNode> childrenAsSortedIntMap(ConfijNode node) {
		Map<String, ConfijNode> children = node.getChildren();
		if (children.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, ConfijNode> sortedResult = new LinkedHashMap<>(children.size());
		children.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(Integer.valueOf(entry.getKey()), entry.getValue()))
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> sortedResult.put(entry.getKey(), entry.getValue()));
		int lowestIndex = sortedResult.keySet()
				.iterator()
				.next();
		if (lowestIndex < 0) {
			throw new IllegalArgumentException("negative indexes are not allowed in list-like, but found: " + lowestIndex);
		}
		return sortedResult;
	}

	/**
	 * finds all children of a node (which are organized as a map) and returns them as a continuous List.
	 *
	 * @param parentNode the parent node of which we want the children
	 * @return the List of child nodes (without null elements)
	 */
	public static List<ConfijNode> childrenAsContinuousList(ConfijNode parentNode) {
		Map<String, ConfijNode> children = parentNode.getChildren();
		if (children.isEmpty()) {
			return Collections.emptyList();
		}
		List<ConfijNode> sortedResult = new ArrayList<>(children.size());
		for (int i = 0; i < children.size(); i++) {
			String key = String.valueOf(i);
			if (!children.containsKey(key)) {
				throw new ConfijBindingException("expected a continuous list, but node {} is missing a child named '{}'", parentNode, key);
			}
			sortedResult.add(children.get(key));
		}
		return sortedResult;
	}
}
