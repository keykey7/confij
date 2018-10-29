package ch.kk7.confij.format.resolve;

import ch.kk7.confij.common.Config4jException;
import ch.kk7.confij.source.simple.ConfijNode;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultResolver implements IVariableResolver {
	private char escapeChar = '\\';
	private final Map<ConfijNode, String> resolvedLeaves = new HashMap<>();
	private final Set<ConfijNode> inProgressLeaves = new HashSet<>();

	@Override
	public String resolve(ConfijNode leaf) {
		clearCache();
		return resolveString(leaf);
	}

	protected String resolveString(ConfijNode leaf) {
		String value = leaf.getValue();
		if (resolvedLeaves.containsKey(leaf)) {
			return resolvedLeaves.get(leaf);
		}
		if (inProgressLeaves.contains(leaf)) {
			throw new Config4jException("circular dependency: cannot resolveString leaf value");
		}
		inProgressLeaves.add(leaf);
		String resolvedValue = resolveString(leaf, value);
		inProgressLeaves.remove(leaf);
		resolvedLeaves.put(leaf, resolvedValue);
		return resolvedValue;
	}

	@Override
	public String resolve(ConfijNode baseLeaf, String value) {
		clearCache();
		return resolveString(baseLeaf, value);
	}

	protected String resolveVariable(ConfijNode baseLeaf, String variableName) {
		// allows for recursive variable names like ${x${y}}
		String pathToLeaf = resolveString(baseLeaf, variableName);
		return resolveStaticForAbsolutePaths(pathToLeaf).orElseGet(() -> {
			// variable must represent a path to a leaf now (usually relative)
			ConfijNode leaf = baseLeaf.resolve(pathToLeaf);
			// further resolve the content of this leaf
			return resolveString(leaf);
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

	protected String resolveString(ConfijNode baseLeaf, String value) {
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
					String resolved = escape(resolveVariable(baseLeaf, toResolve));
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
