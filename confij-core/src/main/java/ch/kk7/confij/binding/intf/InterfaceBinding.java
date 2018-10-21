package ch.kk7.confij.binding.intf;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.simple.SimpleConfig;
import ch.kk7.confij.source.simple.SimpleConfigMap;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import lombok.ToString;
import lombok.Value;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@ToString
public class InterfaceBinding<T> implements ConfigBinding<T> {
	private final Map<String, AttributeInformation> siblingsByName;
	private final InterfaceProxyBuilder<T> interfaceBuilder;

	public InterfaceBinding(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType baseType = bindingType.getResolvedType();
		if (!(baseType instanceof ResolvedInterfaceType)) {
			throw new IllegalArgumentException("expected type " + baseType + " to be a " + ResolvedInterfaceType.class);
		}
		siblingsByName = new HashMap<>();
		interfaceBuilder = new InterfaceProxyBuilder<>((ResolvedInterfaceType) baseType);
		for (ResolvedMethod method : interfaceBuilder.getAllowedMethods()) {
			BindingType methodBindingType = bindingType.bindingFor(method.getReturnType(), bindingType.getBindingSettings()
					.settingsFor(method.getRawMember()));
			ConfigBinding<?> methodDescription = configBinder.toConfigBinding(methodBindingType);
			String configKey = AnnotationUtil.findAnnotation(method.getRawMember(), Key.class).map(Key::value)
					.orElse(method.getName());
			siblingsByName.put(configKey, new AttributeInformation(methodDescription, method));
		}
	}

	@Override
	public ConfigFormatMap describe(FormatSettings formatSettings) {
		FormatSettings settingsForThisClass = formatSettings.settingsFor(interfaceBuilder.getType()
				.getErasedType());
		// TODO: support a special method like: @UnknownValues Map<String,Object> unknowns;
		return ConfigFormatMap.fixedKeysMap(settingsForThisClass, siblingsByName.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
						.getDescription()
						.describe(settingsForThisClass.settingsFor(e.getValue()
								.getMethod()
								.getRawMember())))));
	}

	@Override
	public T bind(SimpleConfig config) {
		if (!(config instanceof SimpleConfigMap)) {
			throw new IllegalStateException("expected a config map, but got: " + config);
		}
		Map<String, SimpleConfig> configMap = ((SimpleConfigMap) config).map();
		InterfaceProxyBuilder<T>.ValidatingProxyBuilder builder = interfaceBuilder.builder();

		siblingsByName.forEach((key, siblingDescription) -> {
			Object siblingValue = siblingDescription.getDescription()
					.bind(configMap.get(key));
			// TODO: what does it mean when a siblingValue is null/empty for a default method? should it be called or simply return null?
			// TODO: -> maybe make the behaviour configurable
			builder.methodToValue(siblingDescription.getMethod(), siblingValue);
		});
		return builder.build();
	}

	protected boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Collection) {
			return ((Collection) value).isEmpty();
		}
		if (value instanceof Map) {
			return ((Map) value).isEmpty();
		}
		if (value.getClass()
				.isArray()) {
			return Array.getLength(value) == 0;
		}
		return false;
	}

	@Value
	public static class AttributeInformation {
		private final ConfigBinding<?> description;
		private final ResolvedMethod method;
	}
}
