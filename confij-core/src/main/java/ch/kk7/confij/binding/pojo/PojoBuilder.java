package ch.kk7.confij.binding.pojo;

import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.common.ClassToImplCache;
import ch.kk7.confij.common.Util;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.types.ResolvedObjectType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Comparator.comparing;

/**
 * Analyze the class, generate the config pojo information.
 *
 * @author せいうはん
 * @version 1.0.0, 2020-05-17 20:43
 * @since 1.0.0, 2020-05-17 20:43
 */
@Getter
@ToString
public class PojoBuilder<T> {

	private final ResolvedObjectType type;

	private final Set<ResolvedField> allowedFields;

	public PojoBuilder(ResolvedObjectType type) {
		this.type = type;
		this.allowedFields = supportedFields(type);
	}

	protected Set<ResolvedField> supportedFields(ResolvedObjectType objectType) {
		ResolvedField[] memberMethodsArr = supportedMemberResolver()
			.resolve(objectType, null, null)
			.getMemberFields();
		Set<ResolvedField> fieldHashSet = new HashSet<>(Arrays.asList(memberMethodsArr));

		ResolvedObjectType parentClass = objectType.getParentClass();
		if (!Util.rawObjectType.equals(parentClass)) {
			Set<ResolvedField> resolvedFields = supportedFields(parentClass);
			fieldHashSet.addAll(resolvedFields);
		}
		return Collections.unmodifiableSet(fieldHashSet);
	}

	protected MemberResolver supportedMemberResolver() {
		MemberResolver memberResolver = new MemberResolver(new TypeResolver());
		memberResolver.setFieldFilter(field -> {
			// We don't support the raw object type.
			if (Util.rawObjectType.equals(field.getDeclaringType())) {
				return false;
			}
			Field rawField = field.getRawMember();
			if ((rawField.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
				return false;
			}

			return !Modifier.isStatic(rawField.getModifiers());
		});
		return memberResolver;
	}

	public ValidatingPojoBuilder builder() {
		return new ValidatingPojoBuilder();
	}

	public class ValidatingPojoBuilder {

		private final Map<ResolvedField, Object> fieldToValues = new HashMap<>();

		public void fieldToValue(ResolvedField resolvedField, Object value) {
			fieldToValues.put(resolvedField, value);
		}

		@SneakyThrows
		public T build() {
			Set<ResolvedField> inputMethods = fieldToValues.keySet();
			Set<ResolvedField> notAllowedMethods = new HashSet<>(inputMethods);
			notAllowedMethods.removeAll(allowedFields);
			if (!notAllowedMethods.isEmpty()) {
				throw new ConfijBindingException("Cannot create instance of type '{}' with fields {}, allowed are {}",
					type, notAllowedMethods, allowedFields);
			}

			// input methods are valid at this point
			Map<Field, Object> fixedFieldToValues = new TreeMap<>(comparing(Field::getName));
			fieldToValues.forEach((field, value) -> {
				fixedFieldToValues.put(field.getRawMember(), value);
			});

			if (fixedFieldToValues.isEmpty()) {
				// This field don't have a configuration.
				return null;
			}

			Class<?> forPojo = type.getErasedType();
			Object instance = ClassToImplCache.getInstance(forPojo);

			for (Map.Entry<Field, Object> fieldToValue : fixedFieldToValues.entrySet()) {
				setFieldValue(fieldToValue.getKey(), instance, fieldToValue.getValue());
			}

			// noinspection unchecked
			return (T) instance;
		}
	}

	@SneakyThrows
	private static void setFieldValue(Field field, Object instance, Object value) {
		if (value != null) {
			field.setAccessible(true);
			field.set(instance, value);
		}
	}
}
