package ch.kk7.confij.format.resolve;

import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.source.tree.ConfijNode;
import lombok.ToString;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: make ThreadSafe
@ToString
public class DefaultResolver implements IVariableResolver {
	private char escapeChar = '\\';
	private String pathSeparator = ".";
	private final Map<ConfijNode, String> resolvedLeaves = new HashMap<>();
	private final Set<ConfijNode> inProgressLeaves = new LinkedHashSet<>();

	@Override
	public String resolveLeaf(ConfijNode leaf) {
		if (leaf.getValue() == null) {
			return null;
		}
		clearCache();
		return resolveLeafInternal(leaf);
	}

	protected String resolveLeafInternal(ConfijNode leaf) {
		String value = leaf.getValue();
		if (value == null) {
			throw new Config4jException("referenced property {} is null", leaf);
		}
		if (resolvedLeaves.containsKey(leaf)) {
			return resolvedLeaves.get(leaf);
		}
		if (inProgressLeaves.contains(leaf)) {
			throw new Config4jException("circular dependency: cannot resolve leaf value. Call stack: {}", inProgressLeaves);
		}
		inProgressLeaves.add(leaf);
		String resolvedValue = resolveValueInternal(leaf, value);
		inProgressLeaves.remove(leaf);
		resolvedLeaves.put(leaf, resolvedValue);
		return resolvedValue;
	}

	@Override
	public String resolveValue(ConfijNode baseNode, String value) {
		clearCache();
		return resolveValueInternal(baseNode, value);
	}

	protected URI pathToUri(String pathToLeaf) {
		String stringUri = Arrays.stream(pathToLeaf.split(Pattern.quote(pathSeparator)))
				.map(ConfijNode::uriEncode)
				.collect(Collectors.joining("/"));
		return URI.create(stringUri);
	}

	protected String resolveVariable(ConfijNode baseLeaf, String variableName) {
		// allows for recursive variable names like ${x${y}}
		String pathToLeaf = resolveValueInternal(baseLeaf, variableName);
		return resolveStaticForAbsolutePaths(pathToLeaf).orElseGet(() -> {
			// variable must represent a path to a leaf now (usually relative)
			URI uriToLeaf = pathToUri(pathToLeaf);
			ConfijNode leaf = baseLeaf.resolve(uriToLeaf);
			// further resolveLeaf the content of this leaf
			return resolveLeafInternal(leaf);
		});
	}

	protected Optional<String> resolveStaticForAbsolutePaths(String uriStr) {
		URI targetUri = URI.create(uriStr);
		String scheme = targetUri.getScheme();
		if (scheme == null) {
			return Optional.empty();
		}
		String path = targetUri.getSchemeSpecificPart();
		switch (scheme) {
			case "env":
				return Optional.ofNullable(System.getenv(path));
			case "sys":
				return Optional.ofNullable(System.getProperty(path));
			default:
				return Optional.empty();
		}
	}

	protected void clearCache() {
		resolvedLeaves.clear();
		inProgressLeaves.clear();
	}

	protected String resolveValueInternal(ConfijNode baseNode, String value) {
		boolean isEscape = false;
		boolean wasDollar = false;
		int bracketCount = 0;
		int bracketContentStart = -1;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (isEscape) {
				// burn the escape char
				value = value.substring(0, i - 1) + value.substring(i);
				i--;
				isEscape = false;
				continue;
			}
			if (c == '$') {
				wasDollar = true;
				continue;
			}
			if (c == escapeChar) {
				isEscape = true;
				wasDollar = false;
				continue;
			}
			if (wasDollar && c == '{') {
				if (bracketCount == 0) {
					bracketContentStart = i + 1;
				}
				bracketCount++;
				wasDollar = false;
				continue;
			}
			if (bracketCount > 0 && c == '}') {
				bracketCount--;
				if (bracketCount == 0) {
					String toResolve = value.substring(bracketContentStart, i);
					String resolved = escape(resolveVariable(baseNode, toResolve));
					String beforeReplacement = value.substring(0, bracketContentStart - 2);
					String afterReplacement = value.substring(i + 1);
					i = beforeReplacement.length() + resolved.length();
					value = beforeReplacement + resolved + afterReplacement;
				}
			}
			wasDollar = false;
		}
		return value;
	}

	protected String escape(String s) {
		s = s.replace("" + escapeChar, "" + escapeChar + escapeChar);
		return s.replaceAll("\\$\\{", escapeChar + "${");
	}

}
