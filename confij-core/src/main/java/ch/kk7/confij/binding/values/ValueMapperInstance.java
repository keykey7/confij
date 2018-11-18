package ch.kk7.confij.binding.values;

@FunctionalInterface
public interface ValueMapperInstance<T> {
	T fromString(String string);

	@FunctionalInterface
	interface NullableValueMapperInstance<T> extends ValueMapperInstance<T> {
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
