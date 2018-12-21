package ch.kk7.confij.binding;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.lang.reflect.Type;

@Value
@NonFinal
public class BindingType {
	@NonNull ResolvedType resolvedType;
	@NonNull BindingContext bindingContext;
	@ToString.Exclude
	@NonNull TypeResolver typeResolver;

	public static BindingType newBindingType(Type forType) {
		return newBindingType(forType, BindingContext.newDefaultContext());
	}

	public static BindingType newBindingType(Type forType, BindingContext bindingContext) {
		TypeResolver typeResolver = new TypeResolver();
		return new BindingType(typeResolver.resolve(forType), bindingContext, typeResolver);
	}

	public BindingType bindingFor(ResolvedType resolvedType) {
		return new BindingType(resolvedType, bindingContext, typeResolver);
	}

	public BindingType bindingFor(ResolvedType resolvedType, BindingContext bindingContext) {
		return new BindingType(resolvedType, bindingContext, typeResolver);
	}
}
