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
		String aString();

		int aPrimitive();
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

	@Test
	public void callUninitializedMethod() {
		assertThrows(BindingException.class, () -> validIntf.aString());
		assertThat(validIntf.toString()).doesNotContain("aString");
	}

	@Test
	public void bindAndCallString() {
		String whatever = UUID.randomUUID() + "";
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aString"), whatever);
		assertThat(validIntf.aString()).isEqualTo(whatever);
		assertThat(validIntf.toString()).contains(whatever);
	}

	@Test
	public void uninitializedPrimitive() {
		assertThrows(BindingException.class, () -> validIntf.aPrimitive());
	}

	@Test
	public void nullPrimitive() {
		validHandler.setMethod(resolveMethod(ValidIntf.class, "aPrimitive"), null);
		assertThat(validIntf.aPrimitive()).isEqualTo(0);
		System.out.println(validIntf.toString());
	}
}
