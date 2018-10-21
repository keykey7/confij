package ch.kk7.confij.binding.intf;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

public abstract class AbstractProxyBuilderTest<T> implements WithAssertions {
	private final TypeResolver typeResolver = new TypeResolver();
	private final ResolvedInterfaceType intfType = (ResolvedInterfaceType) typeResolver.resolve(interfaceClass());
	protected InterfaceProxyBuilder<T> handler;
	protected InterfaceProxyBuilder<T>.ValidatingProxyBuilder builder;

	abstract protected Class<T> interfaceClass();

	@BeforeEach
	public void initHandler() {
		handler = new InterfaceProxyBuilder<>(intfType);
		builder = newBuilder();
	}

	protected InterfaceProxyBuilder<T>.ValidatingProxyBuilder newBuilder() {
		return handler.builder();
	}

	protected T instance() {
		return builder.build();
	}

	protected ResolvedMethod resolveMethod(String methodName) {
		return Arrays.stream(new MemberResolver(typeResolver).resolve(typeResolver.resolve(interfaceClass()), null, null)
				.getMemberMethods())
				.filter(resolvedMethod -> methodName.equals(resolvedMethod.getRawMember()
						.getName()))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("unknown method " + methodName));
	}

	protected InterfaceProxyBuilder<T>.ValidatingProxyBuilder set(String methodName, Object value) {
		return builder.methodToValue(resolveMethod(methodName), value);
	}
}
