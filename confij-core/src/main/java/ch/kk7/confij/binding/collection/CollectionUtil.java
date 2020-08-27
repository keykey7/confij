package ch.kk7.confij.binding.collection;

import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.tree.ConfijNode;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class CollectionUtil {
	/**
	 * finds all children of a node (which are organized as a map) and returns them as a continuous List.
	 *
	 * @param parentNode the parent node of which we want the children
	 * @return the List of child nodes (without null elements)
	 */
	public List<ConfijNode> childrenAsContinuousList(ConfijNode parentNode) {
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
