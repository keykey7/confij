package ch.kk7.config4j.binding.leaf.mapper;

import com.fasterxml.classmate.ResolvedType;

import java.util.Optional;

public interface ValueMapperFactory {
	Optional<ValueMapper<?>> maybeForType(ResolvedType type);
}
