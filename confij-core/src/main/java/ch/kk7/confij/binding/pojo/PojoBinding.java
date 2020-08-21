package ch.kk7.confij.binding.pojo;

import ch.kk7.confij.annotation.Key;
import ch.kk7.confij.binding.BindingContext;
import ch.kk7.confij.binding.BindingResult;
import ch.kk7.confij.binding.BindingType;
import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBinding;
import ch.kk7.confij.common.AnnotationUtil;
import ch.kk7.confij.tree.ConfijNode;
import ch.kk7.confij.tree.NodeBindingContext;
import ch.kk7.confij.tree.NodeDefinition;
import ch.kk7.confij.tree.NodeDefinition.NodeDefinitionMap;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.types.ResolvedObjectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * The binding for bean class, this is the main entry point for our lighting module.
 *
 * @author せいうはん
 * @version 1.0.0, 2020-05-17 08:40
 * @since 1.0.0, 2020-05-17 08:40
 */
@ToString
public class PojoBinding<T> implements ConfigBinding<T> {

	private final Map<String, AttributeInformation> siblingsByName;

	private final PojoBuilder<T> pojoBuilder;

	public PojoBinding(BindingType bindingType, ConfigBinder configBinder) {
		ResolvedType baseType = bindingType.getResolvedType();
		if (!(baseType instanceof ResolvedObjectType)) {
			throw new IllegalArgumentException("expected type " + baseType + " to be a " + ResolvedObjectType.class);
		}
		// resolve context first for class (annotations that hold for the whole class)
		BindingContext classBindingContext = bindingType.getBindingContext().settingsFor(baseType.getErasedType(), false);
		this.siblingsByName = new LinkedHashMap<>();
		this.pojoBuilder = new PojoBuilder<>((ResolvedObjectType) baseType);
		for (ResolvedField field : pojoBuilder.getAllowedFields()) {
			BindingContext bindingContext = classBindingContext.settingsFor(field.getRawMember(), true);
			BindingType methodBindingType = bindingType.bindingFor(field.getType(), bindingContext);
			ConfigBinding<?> methodDescription = configBinder.toConfigBinding(methodBindingType);
			String configKey = AnnotationUtil.findAnnotation(field.getRawMember(), Key.class).map(Key::value)
				.orElse(field.getName());

			this.siblingsByName.put(configKey, new AttributeInformation(methodDescription, field));
		}
	}

	@Override
	public NodeDefinition describe(NodeBindingContext nodeBindingContext) {
		NodeBindingContext settingsForThisClass = nodeBindingContext.settingsFor(pojoBuilder.getType().getErasedType());
		Map<String, NodeDefinition> definitionMap = siblingsByName.entrySet()
			.stream()
			.collect(toMap(
				Map.Entry::getKey,
				e -> e.getValue()
					.getDescription()
					.describe(settingsForThisClass.settingsFor(e.getValue().getField().getRawMember()))
			));

		return NodeDefinitionMap.fixedKeysMap(settingsForThisClass, definitionMap);
	}

	@Override
	public BindingResult<T> bind(ConfijNode config) {
		Map<String, ConfijNode> childConfigs = config.getChildren();
		PojoBuilder<T>.ValidatingPojoBuilder builder = pojoBuilder.builder();
		siblingsByName.forEach((key, siblingDescription) -> {
			Object siblingValue = siblingDescription.getDescription().bind(childConfigs.get(key));
			ResolvedField field = siblingDescription.getField();
			if (Modifier.isStatic(field.getRawMember().getModifiers()) || isEmpty(siblingValue)) {
				return;
			}
			builder.fieldToValue(field, siblingValue);
		});

		T result = builder.build();
		return BindingResult.ofLeaf(result, config);
	}

	protected boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		}
		if (value instanceof Map) {
			return ((Map<?, ?>) value).isEmpty();
		}
		if (value.getClass().isArray()) {
			return Array.getLength(value) == 0;
		}
		return false;
	}

	@Getter
	@AllArgsConstructor
	public static class AttributeInformation {

		private final ConfigBinding<?> description;

		private final ResolvedField field;
	}
}
