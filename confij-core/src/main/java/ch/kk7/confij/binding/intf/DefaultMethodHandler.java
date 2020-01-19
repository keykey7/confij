package ch.kk7.confij.binding.intf;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.UtilityClass;

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
@UtilityClass
public class DefaultMethodHandler {
	private static final Method privateLookupIn = privateLookupIn();

	public static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws DefaultMethodException {
		if (privateLookupIn == null) {
			// assuming java8 at this point
			return invokeDefaultMethodJava8(proxy, method, args);
		} else {
			return invokeDefaultMethodJava9(proxy, method, args);
		}
	}

	private static Object invokeDefaultMethodJava8(Object proxy, Method method, Object[] args) throws DefaultMethodException {
		Class<?> forClass = method.getDeclaringClass();
		Lookup lookup = lookupJava8(forClass);
		try {
			return lookup.in(forClass)
					.unreflectSpecial(method, forClass)
					.bindTo(proxy)
					.invokeWithArguments(args);
		} catch (Throwable throwable) {
			throw new DefaultMethodException(throwable);
		}
	}

	private static Object invokeDefaultMethodJava9(Object proxy, Method method, Object[] args) throws DefaultMethodException {
		// FIXME: this is horrible, cleanup
		Class<?> forClass = method.getDeclaringClass();
		try {
			Lookup lookup = (Lookup) privateLookupIn.invoke(null, forClass, MethodHandles.lookup());
			MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			return lookup.findSpecial(forClass, method.getName(), methodType, forClass)
					.bindTo(proxy)
					.invokeWithArguments(args);
		} catch (Throwable throwable) {
			throw new DefaultMethodException(throwable);
		}
	}

	/**
	 * Java8 only hack to use a lookup of a non-private-accessible interface
	 */
	private static Lookup lookupJava8(Class<?> forClass) throws DefaultMethodException {
		try {
			Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
			constructor.setAccessible(true);
			return constructor.newInstance(forClass);
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new DefaultMethodException(e);
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

	@Value
	@EqualsAndHashCode(callSuper = false)
	protected static class DefaultMethodException extends Exception {
		private final Throwable throwable;
	}
}
