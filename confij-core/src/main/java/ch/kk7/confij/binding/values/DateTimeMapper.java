package ch.kk7.confij.binding.values;

import ch.kk7.confij.annotation.ValueMapper;
import ch.kk7.confij.binding.BindingType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Optional;

public class DateTimeMapper implements ValueMapperFactory {
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@ValueMapper(DateTimeMapper.class)
	public @interface DateTime {
		/**
		 * @return A pattern for {@link DateTimeFormatter#ofPattern(String, Locale)}
		 */
		String value() default "yyyy-MM-dd'T'HH:mm:ss.SXXX";

		/**
		 * @return A locale string for {@link DateTimeFormatter#ofPattern(String, Locale)}
		 */
		String lang() default "";
	}

	@DateTime
	private static final class AnnonHolder {
	}

	@RequiredArgsConstructor
	public static class OffsetDateTimeMapperInstance implements ValueMapperInstance<OffsetDateTime> {
		@NonNull
		private final DateTimeFormatter dateTimeFormatter;

		@Override
		public OffsetDateTime fromString(String string) {
			return OffsetDateTime.parse(string, dateTimeFormatter);
		}
	}

	@RequiredArgsConstructor
	public static class DateMapperInstance implements ValueMapperInstance<Date> {
		@NonNull
		private final OffsetDateTimeMapperInstance mapperInstance;

		@Override
		public Date fromString(String string) {
			return Date.from(mapperInstance.fromString(string)
					.toInstant());
		}
	}

	@Override
	public Optional<ValueMapperInstance<?>> maybeForType(BindingType bindingType) {
		Class<?> type = bindingType.getResolvedType()
				.getErasedType();
		if (type.equals(Date.class)) {
			return Optional.of(newDateMapperInstance(bindingType));
		}
		if (type.equals(OffsetDateTime.class)) {
			return Optional.of(newOffsetDateTimeMapperInstance(bindingType));
		}
		return Optional.empty();
	}

	protected DateMapperInstance newDateMapperInstance(BindingType bindingType) {
		return new DateMapperInstance(newOffsetDateTimeMapperInstance(bindingType));
	}

	protected OffsetDateTimeMapperInstance newOffsetDateTimeMapperInstance(BindingType bindingType) {
		DateTime dateTime = bindingType.getBindingContext()
				.getFactoryConfigFor(DateTimeMapper.class)
				.filter(DateTime.class::isInstance)
				.map(DateTime.class::cast)
				.orElse(AnnonHolder.class.getAnnotation(DateTime.class));
		final Locale formatLang = dateTime.lang()
				.isEmpty() ? Locale.getDefault(Category.FORMAT) : Locale.forLanguageTag(dateTime.lang());
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTime.value(), formatLang);
		return new OffsetDateTimeMapperInstance(dateTimeFormatter);
	}
}
