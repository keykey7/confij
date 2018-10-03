package ch.kk7.confij.binding.leaf;

@FunctionalInterface
public interface IValueMapper<T> {
	T fromString(String string);

	@FunctionalInterface
	interface NullableValueMapper<T> extends IValueMapper<T> {
		@Override
		default T fromString(String string) {
			if (string == null) {
				return null;
			}
			return fromNonNullString(string);
		}

		T fromNonNullString(String string);
	}
}
