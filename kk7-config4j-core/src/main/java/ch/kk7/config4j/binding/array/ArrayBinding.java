package ch.kk7.config4j.binding.array;

import ch.kk7.config4j.binding.BindingType;
import ch.kk7.config4j.binding.ConfigBinder;
import ch.kk7.config4j.binding.ConfigBinding;
import ch.kk7.config4j.binding.ConfigBinding.BindResult.BindResultBuilder;
import ch.kk7.config4j.format.ConfigFormat.ConfigFormatList;
import ch.kk7.config4j.format.FormatSettings;
import ch.kk7.config4j.source.simple.SimpleConfig;
import ch.kk7.config4j.source.simple.SimpleConfigList;
import com.fasterxml.classmate.ResolvedType;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayBinding<T> implements ConfigBinding<Object> {
	private final ResolvedType componentType;
	private final ConfigBinding<T> componentDescription;

	public ArrayBinding(BindingType bindingType, ConfigBinder configBinder) {
		componentType = bindingType.getResolvedType();
		//noinspection unchecked
		componentDescription = (ConfigBinding<T>) configBinder.toConfigBinding(bindingType);
	}

	@Override
	public ConfigFormatList describe(FormatSettings formatSettings) {
		return new ConfigFormatList(formatSettings, componentDescription.describe(formatSettings));
	}

	/**
	 * binds to Object instead of T[] since it also handles primitive arrays
	 */
	@Override
	public BindResult<Object> bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigList)) {
			throw new IllegalStateException("expected a config list, but got: " + config);
		}
		BindResultBuilder<Object> resultBuilder = BindResult.builder();
		List<SimpleConfig> configList = ((SimpleConfigList) config).list();
		Object array = Array.newInstance(componentType.getErasedType(), configList.size());
		int i = 0;
		for (SimpleConfig configItem : configList) {
			BindResult<T> itemResult = componentDescription.bind(configItem);
			Array.set(array, i++, itemResult.getValue());
			resultBuilder.sibling(String.valueOf(i), itemResult);
		}
		return resultBuilder.value(array)
				.build();
	}
}
