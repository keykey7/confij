package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.ConfijBindingException;
import ch.kk7.confij.binding.intf.DefaultMethodHandler.DefaultMethodException;
import ch.kk7.confij.binding.intf.InterfaceProxyBuilder.ConfijHandled;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SuppressWarnings("ProhibitedExceptionDeclared,squid:S00112")
@AllArgsConstructor
@ToString
public class IntfaceInvocationHandler implements InvocationHandler, Serializable {
	private String className;
	@SuppressWarnings("java:S1948") // serialization is possible IF all values are Serializable
	private Map<Method, Object> methodToValues;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			return invokeObjectClass(proxy, method, args);
		}
		if (ConfijHandled.class.equals(method.getDeclaringClass())) {
			return invokeConfijHandled(proxy, method, args);
		}
		if (methodToValues.containsKey(method)) {
			return methodToValues.get(method);
		}
		if (method.isDefault()) {
			return invokeDefault(proxy, method, args);
		}
		throw new ConfijBindingException("cannot call method '{}' as it was not initialized. initialized are: {}", method,
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

	protected Object invokeConfijHandled(Object proxy, Method method, Object[] args) throws Throwable {
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
		return className +
				"{" +
				methodToValues.entrySet()
						.stream()
						.map(e -> e.getKey()
								.getName() + "=" + e.getValue())
						.collect(Collectors.joining(", ")) +
				"}";
	}

	protected boolean intfEquals(Object proxy, Object o) {
		if (proxy == o) {
			return true;
		}
		if (!(o instanceof InterfaceProxyBuilder.ConfijHandled)) {
			return false;
		}
		ConfijHandled that = (ConfijHandled) o;
		return Objects.equals(methodToValues, that.methodToValue());
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(className);
		out.writeInt(methodToValues.size());
		for (Entry<Method, Object> entry : methodToValues.entrySet()) {
			writeMethodObject(entry.getKey(), out);
			out.writeObject(entry.getValue());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		className = (String) in.readObject();
		int size = in.readInt();
		methodToValues = new TreeMap<>(InterfaceProxyBuilder.methodNameComparator);
		for (int i = 0; i < size; i++) {
			Method method = readMethodObject(in);
			Object value = in.readObject();
			methodToValues.put(method, value);
		}
	}

	private static void writeMethodObject(Method method, ObjectOutputStream out) throws IOException {
		out.writeObject(method.getDeclaringClass());
		out.writeObject(method.getName());
		out.writeObject(method.getParameterTypes());
	}

	private static Method readMethodObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		Class<?> clazz = (Class<?>) in.readObject();
		String name = (String) in.readObject();
		Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IOException("failed to deserialize method " + clazz + "#" + name + "(" + Arrays.asList(parameterTypes) + ")");
		}
	}
}
