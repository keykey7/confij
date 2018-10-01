package ch.kk7.config4j.binding;

import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

public interface ConfigBinding<T> {
	ConfigFormat describe(FormatSettings formatSettings);

	BindResult<T> bind(SimpleConfig config);

	@Value
	@Builder
	class BindResult<T> {
		private T value;
		@Singular
		private Map<String, BindResult<?>> siblings;
	}
}
