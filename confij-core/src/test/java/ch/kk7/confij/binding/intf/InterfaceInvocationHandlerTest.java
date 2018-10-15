package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.BindingException;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InterfaceInvocationHandlerTest implements WithAssertions {
	private final TypeResolver typeResolver = new TypeResolver();
	private final ResolvedInterfaceType validIntfType = (ResolvedInterfaceType) typeResolver.resolve(ValidIntf.class);

	private interface ValidIntf {
		String STATIC = "static";

		String aString();

		int aPrimitive();

		static String aStatic() {
			return "static";
		}

		default String aDefault() {
			return "default";
		}

		default String aDefaultThrows() {
			throw new IllegalStateException("always doing this");
		}
	}

	private InterfaceInvocationHandler<ValidIntf> validHandler;
	private ValidIntf validIntf;

	@BeforeEach
	public void initHandler() {
		validHandler = new InterfaceInvocationHandler<>(validIntfType);
		validIntf = validHandler.instance();
	}

	private ResolvedMethod resolveMethod(Class intf, String methodName) {
		return Arrays.stream(new MemberResolver(typeResolver).resolve(typeResolver.resolve(intf), null, null)
				.getMemberMethods())
				.filter(resolvedMethod -> methodName.equals(resolvedMethod.getRawMember()
						.getName()))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("unknown method " + methodName));
	}

	private String aString() {
		String whatever = UUID.randomUUID() + "";
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aString"), whatever);
		return whatever;
	}

	private void aDefault(String value) {
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aDefault"), value);
	}

	private void aDefaultThrows(String value) {
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aDefaultThrows"), value);
	}

	private void aPrimitive(Object primitive) {
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aPrimitive"), primitive);
	}

	@Test
	public void callUninitializedMethod() {
		assertThrows(BindingException.class, () -> validIntf.aString());
		assertThat(validIntf.toString()).doesNotContain("aString");
	}

	@Test
	public void invalidSetMethod() {
		ResolvedMethod invalidSetMethod = resolveMethod(InterfaceInvocationHandlerTest.class, "invalidSetMethod");
		assertThrows(IllegalArgumentException.class, () -> validHandler.setMethod(invalidSetMethod, ""));
	}

	@Test
	public void toStringForDefinedValue() {
		String whatever = aString();
		assertThat(validIntf.toString()).contains("aString=" + whatever)
				.doesNotContain("aPrimitive") // debatable
				.contains(ValidIntf.class.getSimpleName());
	}

	@Test
	public void bindAndCall() {
		String whatever = aString();
		assertThat(validIntf.aString()).isEqualTo(whatever);
	}

	@Test
	public void uninitializedPrimitive() {
		assertThrows(BindingException.class, () -> validIntf.aPrimitive());
	}

	@Test
	public void nullPrimitive() {
		aPrimitive(null);
		assertThat(validIntf.aPrimitive()).isEqualTo(0);
		System.out.println(validIntf.toString());
	}

	@Test
	public void somePrimitive() {
		aPrimitive(42);
		assertThat(validIntf.aPrimitive()).isEqualTo(42);
	}

	@Test
	public void instanceEqualsUninitialized() {
		ValidIntf validIntf2 = validIntf;
		initHandler(); // new instance with new handler
		assertThat(validIntf2).isNotSameAs(validIntf)
				.isEqualTo(validIntf)
				.isEqualTo(validIntf2)
				.isNotEqualTo(null)
				.isNotEqualTo(new Object());
		assertThat(validIntf2.hashCode()).isEqualTo(validIntf.hashCode());
	}

	@Test
	public void instanceEquals() {
		aString();
		ValidIntf validIntf2 = validIntf;
		initHandler(); // new instance with new handler
		assertThat(validIntf2).isNotEqualTo(validIntf);
		aString();
		assertThat(validIntf2).isNotEqualTo(validIntf);
		assertThat(validIntf2.hashCode()).isNotEqualTo(validIntf.hashCode());
	}

	@Test
	public void objectMethodDoesntCrash() {
		synchronized (validIntf) {
			validIntf.notify();
		}
	}

	@Test
	public void callUninitializedDefaultMethod() {
		assertThrows(Exception.class, () -> validIntf.aDefault());
	}

	@Test
	public void callDefaultMethod() {
		aDefault(null);
		assertThat(validIntf.aDefault()).isEqualTo("default");
	}

	@Test
	public void overrideDefaultMethod() {
		aDefault("new");
		assertThat(validIntf.aDefault()).isEqualTo("new");
	}

	@Test
	public void callThrowingDefaultMethod() {
		assertThrows(BindingException.class, () -> aDefaultThrows(null));
	}

	@Test
	public void callUninitializedStaticMethod() {
		assertThat(ValidIntf.aStatic()).isEqualTo("static");
		assertThat(ValidIntf.STATIC).isEqualTo("static");
	}

	@Test
	public void cannotOverrideStaticMethod() {
		assertThrows(Exception.class, () -> validHandler.setMethod(resolveMethod(ValidIntf.class, "aStatic"), "whatever"));
	}
}
