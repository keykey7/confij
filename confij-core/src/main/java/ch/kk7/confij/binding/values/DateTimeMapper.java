package ch.kk7.confij.binding.values;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class DateTimeMapper implements ValueMapperFactory {
	/**
	 * to be put on any of the supported Temporal types to define the format of the string to be parsed.
	 */
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(DateTimeMapper.class)
	public @interface DateTime {
		/**
		 * @return A pattern for {@link DateTimeFormatter#ofPattern(String, Locale)}.
		 */
		String value() default "";

		/**
		 * @return A locale string for {@link DateTimeFormatter#ofPattern(String, Locale)}
		 */
		String lang() default "us";
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		Class<?> type = bindingType.getResolvedType()
				.getErasedType();
		if (type.equals(ZonedDateTime.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_ZONED_DATE_TIME);
			return Optional.of(x -> ZonedDateTime.parse(x, formatter));
		}
		if (type.equals(OffsetDateTime.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			return Optional.of(x -> OffsetDateTime.parse(x, formatter));
		}
		if (type.equals(Instant.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_INSTANT);
			return Optional.of(x -> formatter.parse(x, Instant::from));
		}
		if (type.equals(LocalDateTime.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return Optional.of(x -> LocalDateTime.parse(x, formatter));
		}
		if (type.equals(LocalDate.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_LOCAL_DATE);
			return Optional.of(x -> LocalDate.parse(x, formatter));
		}
		if (type.equals(LocalTime.class)) {
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_LOCAL_TIME);
			return Optional.of(x -> LocalTime.parse(x, formatter));
		}
		if (type.equals(Date.class)) {
			// a debatable default, but Date is anyway a lost cause...
			DateTimeFormatter formatter = newDateTimeFormatter(bindingType, DateTimeFormatter.ISO_INSTANT);
			return Optional.of(x -> Date.from(formatter.parse(x, Instant::from)));
		}
		return Optional.empty();
	}

	protected DateTimeFormatter newDateTimeFormatter(BindingType bindingType, DateTimeFormatter defaultFormatter) {
		return bindingType.getBindingContext()
				.getFactoryConfigFor(DateTimeMapper.class)
				.filter(DateTime.class::isInstance)
				.map(DateTime.class::cast)
				.map(annon -> {
					DateTimeFormatter formatter = annon.value()
							.isEmpty() ? defaultFormatter : DateTimeFormatter.ofPattern(annon.value());
					final Locale formatLang = Locale.forLanguageTag(annon.lang());
					return formatter.withLocale(formatLang);
				})
				.orElse(defaultFormatter);
	}
}
