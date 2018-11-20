package ch.kk7.confij.binding.intf;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.format.ConfigFormat.ConfigFormatMap;
import ch.kk7.confij.format.FormatSettings;
import ch.kk7.confij.source.tree.ConfijNode;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
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
		siblingsByName = new LinkedHashMap<>();
		interfaceBuilder = new InterfaceProxyBuilder<>((ResolvedInterfaceType) baseType);
		for (ResolvedMethod method : interfaceBuilder.getAllowedMethods()) {
			BindingType methodBindingType = bindingType.bindingFor(method.getReturnType(), bindingType.getBindingContext()
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
	public T bind(ConfijNode config) {
		Map<String, ConfijNode> childConfigs = config.getChildren();
		InterfaceProxyBuilder<T>.ValidatingProxyBuilder builder = interfaceBuilder.builder();
		siblingsByName.forEach((key, siblingDescription) -> {
			Object siblingValue = siblingDescription.getDescription()
					.bind(childConfigs.get(key));
			// TODO: what does it mean when a siblingValue is null/empty for a default method? should it be called or simply return null?
			// TODO: -> maybe make the behaviour configurable
			ResolvedMethod method = siblingDescription.getMethod();
			if (method.getRawMember()
					.isDefault() && isEmpty(siblingValue)) {
				return;
			}
			builder.methodToValue(method, siblingValue);
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

	@AllArgsConstructor
	@Getter
	public static class AttributeInformation {
		private final ConfigBinding<?> description;
		private final ResolvedMethod method;
	}
}
