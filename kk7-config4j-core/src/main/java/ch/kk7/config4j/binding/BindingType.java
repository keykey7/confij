package ch.kk7.config4j.binding;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import java.lang.reflect.Type;

public class BindingType {
	private final ResolvedType resolvedType;
	private final BindingSettings bindingSettings;
	private final TypeResolver typeResolver;

	protected BindingType(ResolvedType resolvedType, BindingSettings bindingSettings, TypeResolver typeResolver) {
		this.resolvedType = resolvedType;
		this.bindingSettings = bindingSettings;
		this.typeResolver = typeResolver;
	}

	public static BindingType newBindingType(Type forType) {
		TypeResolver typeResolver = new TypeResolver();
		return new BindingType(typeResolver.resolve(forType), BindingSettings.newDefaultSettings(), typeResolver);
	}

	public BindingType bindingFor(ResolvedType resolvedType) {
		return new BindingType(resolvedType, bindingSettings, typeResolver);
	}

	public BindingType bindingFor(ResolvedType resolvedType, BindingSettings bindingSettings) {
		return new BindingType(resolvedType, bindingSettings, typeResolver);
	}

	public ResolvedType getResolvedType() {
		return resolvedType;
	}

	public BindingSettings getBindingSettings() {
		return bindingSettings;
	}
}
