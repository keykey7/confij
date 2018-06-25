package ch.kk7.config4j.binding;

import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.format.ConfigFormat;
import ch.kk7.config4j.source.simple.SimpleConfig;

public interface ConfigBinding<T> {
	ConfigFormat describe(FormatSettings formatSettings);

	T bind(SimpleConfig config);
}
