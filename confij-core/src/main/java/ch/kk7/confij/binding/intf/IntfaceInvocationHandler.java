package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingException;
import ch.kk7.confij.binding.intf.DefaultMethodHandler.DefaultMethodException;
import ch.kk7.confij.binding.intf.InterfaceProxyBuilder.Config4jHandled;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ProhibitedExceptionDeclared")
@AllArgsConstructor
@ToString
public class IntfaceInvocationHandler implements InvocationHandler {
	private final String className;
	private final Map<Method, Object> methodToValues;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			return invokeObjectClass(proxy, method, args);
		}
		if (Config4jHandled.class.equals(method.getDeclaringClass())) {
			return invokeConfig4JHandled(proxy, method, args);
		}
		if (methodToValues.containsKey(method)) {
			return methodToValues.get(method);
		}
		if (method.isDefault()) {
			return invokeDefault(proxy, method, args);
		}
		throw new BindingException("cannot call method '{}' as it was not initialized. initialized are: {}", method,
				methodToValues.keySet());
	}

	protected Object invokeObjectClass(Object proxy, Method method, Object[] args) throws Throwable {
		if ("toString".equals(method.getName())) {
			return intfToString();
		}
		if ("equals".equals(method.getName())) {
			return intfEquals(proxy, args[0]);
		}
		if ("hashCode".equals(method.getName())) {
			return methodToValues.hashCode();
		}
		return method.invoke(this, args);
	}

	protected Object invokeConfig4JHandled(Object proxy, Method method, Object[] args) throws Throwable {
		// there is only 1
		return methodToValues;
	}

	protected Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return DefaultMethodHandler.invokeDefaultMethod(proxy, method, args);
		} catch (DefaultMethodException e) {
			throw e.getThrowable();
		}
	}

	protected String intfToString() {
		StringBuilder sb = new StringBuilder(className).append("@")
				.append(Integer.toHexString(hashCode() & 0xffff))
				.append("{");
		methodToValues.forEach((k, v) -> sb.append(k.getName())
				.append("=")
				.append(v)
				.append(", "));
		sb.setLength(sb.length() - 2);
		sb.append("}");
		return sb.toString();
	}

	protected boolean intfEquals(Object proxy, Object o) {
		if (proxy == o) {
			return true;
		}
		if (!(o instanceof Config4jHandled)) {
			return false;
		}
		Config4jHandled that = (Config4jHandled) o;
		return Objects.equals(methodToValues, that.methodToValue());
	}
}
