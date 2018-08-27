package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.leaf.mapper.DefaultValueMapperFactory.BooleanFormatException;
import ch.kk7.config4j.binding.leaf.mapper.DefaultValueMapperFactory.CharFormatException;

public class PrimitiveMapper {
	private PrimitiveMapper() {
		// only statics
	}

	public static boolean parseBoolean(String string) {
		if (string == null || "false".equals(string)) {
			return false;
		}
		if ("true".equals(string)) {
			return true;
		}
		throw BooleanFormatException.forInputString(string);
	}

	public static char parseChar(String string) {
		if (string == null) {
			return '\0';
		}
		if (string.length() != 1) {
			throw CharFormatException.forInputString(string);
		}
		return string.charAt(0);
	}

	public static byte parseByte(String string) {
		if (string == null) {
			return 0;
		}
		return Byte.parseByte(string);
	}

	public static short parseShort(String string) {
		if (string == null) {
			return 0;
		}
		return Short.parseShort(string);
	}

	public static int parseInt(String string) {
		if (string == null) {
			return 0;
		}
		return Integer.parseInt(string);
	}

	public static long parseLong(String string) {
		if (string == null) {
			return 0;
		}
		return Long.parseLong(string);
	}

	public static float parseFloat(String string) {
		if (string == null) {
			return 0;
		}
		return Float.parseFloat(string);
	}

	public static double parseDouble(String string) {
		if (string == null) {
			return 0;
		}
		return Double.parseDouble(string);
	}
}
