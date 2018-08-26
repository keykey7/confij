package ch.kk7.config4j.binding.leaf;

@FunctionalInterface
public interface IValueMapper<T> {
	T fromString(String string);
}
