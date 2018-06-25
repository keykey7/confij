package ch.kk7.config4j.binding.leaf.mapper;

@FunctionalInterface
public interface ValueMapper<T> {
	T fromString(String string);
}
