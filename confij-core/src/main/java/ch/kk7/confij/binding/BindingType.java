package ch.kk7.confij.binding;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import java.lang.reflect.Type;

public class BindingType {
	private final ResolvedType resolvedType;
	private final BindingContext bindingContext;
	private final TypeResolver typeResolver;

	protected BindingType(ResolvedType resolvedType, BindingContext bindingContext, TypeResolver typeResolver) {
		this.resolvedType = resolvedType;
		this.bindingContext = bindingContext;
		this.typeResolver = typeResolver;
	}

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

	public ResolvedType getResolvedType() {
		return resolvedType;
	}

	public BindingContext getBindingContext() {
		return bindingContext;
	}
}
