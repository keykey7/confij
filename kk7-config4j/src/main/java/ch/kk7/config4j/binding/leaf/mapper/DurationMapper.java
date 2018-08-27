package ch.kk7.config4j.binding.leaf.mapper;

import ch.kk7.config4j.binding.BindingException;
import ch.kk7.config4j.binding.leaf.IValueMapper.NullableValueMapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DurationMapper implements NullableValueMapper<Duration> {
	/**
	 * Parses a duration string. If no units are specified in the string, it is
	 * assumed to be in milliseconds. The returned duration is in nanoseconds.
	 * The purpose of this function is to implement the duration-related methods
	 * in the ConfigObject interface.
	 *
	 * @implSpec https://github.com/lightbend/config/blob/ea45ea3767a201933eeeb9c3f0f13eacc9b51f07/config/src/main/java/com/typesafe/config/impl/SimpleConfig.java
	 */
	private static long parseDurationAsNanos(String s) {
		String unitString = getUnits(s);
		String numberString = s.substring(0, s.length() - unitString.length())
				.trim();

		// this would be caught later anyway, but the error message
		// is more helpful if we check it here.
		if (numberString.length() == 0) {
			throw new BindingException("bad value: cannot convert an empty value to a Duration");
		}

		TimeUnit units = stringToTimeUnit(unitString);
		try {
			// if the string is purely digits, parse as an integer to avoid
			// possible precision loss;
			// otherwise as a double.
			if (numberString.matches("[+-]?[0-9]+")) {
				return units.toNanos(Long.parseLong(numberString));
			} else {
				long nanosInUnit = units.toNanos(1);
				return (long) (Double.parseDouble(numberString) * nanosInUnit);
			}
		} catch (NumberFormatException e) {
			throw new BindingException("Could not parse duration number '{}'", numberString);
		}
	}

	private static TimeUnit stringToTimeUnit(String unitString) {
		if (unitString.length() > 2 && !unitString.endsWith("s")) {
			unitString = unitString + "s";
		}

		// note that this is deliberately case-sensitive
		switch (unitString) {
			case "":
			case "ms":
			case "millis":
			case "milliseconds":
				return TimeUnit.MILLISECONDS;
			case "us":
			case "micros":
			case "microseconds":
				return TimeUnit.MICROSECONDS;
			case "ns":
			case "nanos":
			case "nanoseconds":
				return TimeUnit.NANOSECONDS;
			case "d":
			case "days":
				return TimeUnit.DAYS;
			case "h":
			case "hours":
				return TimeUnit.HOURS;
			case "s":
			case "seconds":
				return TimeUnit.SECONDS;
			case "m":
			case "minutes":
				return TimeUnit.MINUTES;
			default:
				throw new BindingException("Could not parse time unit '{}' (try ns, us, ms, s, m, h, d)", unitString);
		}
	}

	public static String getUnits(String s) {
		int i = s.length() - 1;
		while (i >= 0) {
			char c = s.charAt(i);
			if (!Character.isLetter(c)) {
				break;
			}
			i -= 1;
		}
		return s.substring(i + 1);
	}

	@Override
	public Duration fromNonNullString(String string) {
		long nanos = parseDurationAsNanos(string);
		return Duration.ofNanos(nanos);
	}
}
