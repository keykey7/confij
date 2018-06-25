package ch.kk7.config4j.binding.intf;

import ch.kk7.config4j.common.Config4jException;
import ch.kk7.config4j.common.Util;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceInvocationHandler<T> implements InvocationHandler {
	private final ResolvedInterfaceType type;
	private final List<ResolvedMethod> supportedMethods;
	private final Map<Method, Object> methodToValue;

	public interface Config4jHandled {
		// marker interface
	}

	public InterfaceInvocationHandler(ResolvedInterfaceType type) {
		this.type = type;
		supportedMethods = Arrays.asList(newMemberResolver().resolve(type, null, null)
				.getMemberMethods());
		methodToValue = new HashMap<>();
	}

	public ResolvedInterfaceType getType() {
		return type;
	}

	protected MemberResolver newMemberResolver() {
		MemberResolver memberResolver = new MemberResolver(new TypeResolver());
		memberResolver.setMethodFilter(method -> {
			if (isIgnorableMethod(method)) {
				return false;
			}
			if (method.getRawMember()
					.getParameterCount() != 0) {
				throw new Config4jException("expected no-arg methods only, but found " + method);
			}
			return true;
		});
		return memberResolver;
	}

	public List<ResolvedMethod> getSupportedMethods() {
		return supportedMethods;
	}

	private static boolean isIgnorableMethod(RawMethod method) {
		Method raw = method.getRawMember();
		return Util.rawObjectType.equals(method.getDeclaringType()) ||
				method.isStatic() ||
				(raw.getParameterCount() != 0 && raw.isDefault());
	}

	public void clear() {
		methodToValue.clear();
	}

	public void setMethod(ResolvedMethod method, Object value) {
		if (!supportedMethods.contains(method)) {
			throw new IllegalArgumentException("method " + method + " isn't supported");
		}
		methodToValue.put(method.getRawMember(), value);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		if (Object.class.equals(method.getDeclaringClass())) {
			return method.invoke(this, args);
		}
		if (!methodToValue.containsKey(method)) {
			throw new IllegalStateException("unable to handle " + method + ", known are: " + methodToValue.keySet());
		}
		return methodToValue.get(method);
	}

	@SuppressWarnings("unchecked")
	public T newInstance() {
		Class<T> forInterface = (Class<T>) type.getErasedType();
		return (T) Proxy.newProxyInstance(forInterface.getClassLoader(), new Class[]{forInterface, Config4jHandled.class}, this);
	}
}
