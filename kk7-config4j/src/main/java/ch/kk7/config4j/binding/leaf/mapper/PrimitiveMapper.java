package ch.kk7.config4j.binding.leaf.mapper;

public class PrimitiveMapper {
	private PrimitiveMapper() {
		// only statics
	}

	public static boolean parseBoolean(String string) {
		if ("false".equals(string)) {
			return false;
		}
		if ("true".equals(string)) {
			return true;
		}
		throw BooleanFormatException.forInputString(string);
	}

	public static char parseChar(String string) {
		if (string.length() != 1) {
			throw CharFormatException.forInputString(string);
		}
		return string.charAt(0);
	}

	public static class BooleanFormatException extends IllegalArgumentException {
		public BooleanFormatException(String str) {
			super(str);
		}

		static BooleanFormatException forInputString(String str) {
			return new BooleanFormatException("For input string: \"" + str + "\"");
		}
	}

	public static class CharFormatException extends IllegalArgumentException {
		public CharFormatException(String str) {
			super(str);
		}

		static CharFormatException forInputString(String str) {
			return new CharFormatException("For input string: \"" + str + "\"");
		}
	}
}
