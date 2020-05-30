package ch.kk7.confij.common;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.util.ResolvedTypeCache;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class Util {
	private final int typeResolverCacheSize = Integer.parseInt(System.getProperty("ch.kk7.confij.typeResolver.cacheSize", "200"));

	public TypeResolver TYPE_RESOLVER = new TypeResolver(ResolvedTypeCache.lruCache(typeResolverCacheSize));

	public ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);

	public <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}
}
