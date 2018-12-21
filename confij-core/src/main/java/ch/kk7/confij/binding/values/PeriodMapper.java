package ch.kk7.confij.binding.values;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.values.ValueMapperInstance.NullableValueMapperInstance;
import ch.kk7.confij.binding.values.DurationMapper.DurationMapperInstance;

import java.time.DateTimeException;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public class PeriodMapper extends AbstractClassValueMapper<Period> {
	public static class PeriodMapperInstance implements NullableValueMapperInstance<Period> {
		/**
		 * Parses a period string. If no units are specified in the string, it is
		 * assumed to be in days. The returned period is in days.
		 * The purpose of this function is to implement the period-related methods
		 * in the ConfigObject interface.
		 *
		 * Heavily inspired by https://github.com/lightbend/config/blob/ea45ea3767a201933eeeb9c3f0f13eacc9b51f07/config/src/main/java/com/typesafe/config/impl/SimpleConfig.java
		 */
		public static Period parsePeriod(String s) {
			String unitString = DurationMapperInstance.getUnits(s);
			String numberString = s.substring(0, s.length() - unitString.length())
					.trim();
			// this would be caught later anyway, but the error message
			// is more helpful if we check it here.
			if (numberString.length() == 0) {
				throw new BindingException("bad value: cannot convert an empty value to a Period");
			}

			ChronoUnit units = stringToChronoUnit(unitString);
			try {
				return periodOf(Integer.parseInt(numberString), units);
			} catch (NumberFormatException e) {
				throw new BindingException("Could not parse duration number '{}'", numberString, e);
			}
		}

		private static ChronoUnit stringToChronoUnit(String unitString) {
			if (unitString.length() > 2 && !unitString.endsWith("s")) {
				unitString = unitString + "s";
			}
			// note that this is deliberately case-sensitive
			switch (unitString) {
				case "":
				case "d":
				case "days":
					return ChronoUnit.DAYS;
				case "w":
				case "weeks":
					return ChronoUnit.WEEKS;
				case "m":
				case "mo":
				case "months":
					return ChronoUnit.MONTHS;
				case "y":
				case "years":
					return ChronoUnit.YEARS;
				default:
					throw new BindingException("Could not parse time unit '{}' (try d, w, mo, y)", unitString);
			}
		}

		private static Period periodOf(int n, ChronoUnit unit) {
			if (unit.isTimeBased()) {
				throw new DateTimeException(unit + " cannot be converted to a java.time.Period");
			}
			switch (unit) {
				case DAYS:
					return Period.ofDays(n);
				case WEEKS:
					return Period.ofWeeks(n);
				case MONTHS:
					return Period.ofMonths(n);
				case YEARS:
					return Period.ofYears(n);
				default:
					throw new DateTimeException(unit + " cannot be converted to a java.time.Period");
			}
		}

		@Override
		public Period fromNonNullString(String string) {
			return parsePeriod(string);
		}
	}

	public PeriodMapper() {
		super(Period.class);
	}

	@Override
	public ValueMapperInstance<Period> newInstance(BindingType bindingType) {
		return new PeriodMapperInstance();
	}
}
