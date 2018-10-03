package ch.kk7.confij.binding.intf;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * invoke default methods on interfaces.
 * inspired by: https://blog.jooq.org/2018/03/28/correct-reflective-access-to-interface-default-methods-in-java-8-9-10/
 */
public class DefaultMethodHandler {
	private static final Method privateLookupIn = privateLookupIn();

	private DefaultMethodHandler() {
		// util thingie
	}

	public static Object invokeDefaultMethod(Object proxy, Method method,
			Object[] args) throws InvocationTargetException, IllegalAccessException {
		if (privateLookupIn == null) {
			// assuming java8 at this point
			return invokeDefaultMethodJava8(proxy, method, args);
		} else {
			return invokeDefaultMethodJava9(proxy, method, args);
		}
	}

	private static Object invokeDefaultMethodJava8(Object proxy, Method method,
			Object[] args) throws InvocationTargetException, IllegalAccessException {
		Class<?> forClass = method.getDeclaringClass();
		Lookup lookup = lookupJava8(forClass);
		try {
			return lookup.in(forClass)
					.unreflectSpecial(method, forClass)
					.bindTo(proxy)
					.invokeWithArguments(args);
		} catch (Throwable throwable) {
			throw new InvocationTargetException(throwable);
		}
	}

	private static Object invokeDefaultMethodJava9(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		// FIXME: this is horrible, cleanup
		Class<?> forClass = method.getDeclaringClass();
		Lookup lookup = (Lookup) privateLookupIn.invoke(forClass, MethodHandles.lookup());
		try {
			return lookup.findSpecial(forClass, method.getName(), MethodType.methodType(method.getReturnType(), new Class[0]), forClass)
					.bindTo(proxy)
					.invokeWithArguments(args);
		} catch (Throwable throwable) {
			throw new InvocationTargetException(throwable);
		}
	}

	/**
	 * Java8 only hack to use a lookup of a non-private-accessible interface
	 */
	private static Lookup lookupJava8(Class<?> forClass) throws InvocationTargetException, IllegalAccessException {
		try {
			Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
			constructor.setAccessible(true);
			return constructor.newInstance(forClass);
		} catch (InstantiationException | NoSuchMethodException e) {
			throw new InvocationTargetException(e);
		}
	}

	@SuppressWarnings("JavaReflectionMemberAccess")
	private static Method privateLookupIn() {
		try {
			return MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
		} catch (NoSuchMethodException e) {
			// TODO: log me somehow: assuming java < 9
		}
		return null;
	}
}
