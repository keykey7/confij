package ch.kk7.confij.common;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.util.ResolvedTypeCache;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class Util {
	private final int typeResolverCacheSize = Integer.parseInt(System.getProperty("ch.kk7.confij.typeResolver.cacheSize", "200"));
	public TypeResolver TYPE_RESOLVER = new TypeResolver(ResolvedTypeCache.lruCache(typeResolverCacheSize));
	public ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);

	public <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	public Optional<String> getScheme(String uriish) {
		String[] schemeParts = uriish.split(":", 2);
		if (schemeParts.length == 1) {
			return Optional.empty();
		}
		return Optional.of(schemeParts[0]);
	}

	public String getSchemeSpecificPart(String uriish) {
		final String[] schemeParts = uriish.split(":", 2);
		final String path =  schemeParts[schemeParts.length - 1];
		final String[] pathParts = path.split("#", 2);
		return pathParts[0];
	}

	public Optional<String> getFragment(String uriish) {
		final String[] pathParts = uriish.split("#", 2);
		return pathParts.length == 1 ? Optional.empty() : Optional.of(pathParts[1]);
	}
}
