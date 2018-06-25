package ch.kk7.config4j.provider;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigInvocationHandlerTest {
	private interface TestIntf {
		int anInt();

		Integer anInteger();

		String aString();

		Object anObject();

		List<?> aRawList();

		List<String> aStringList();

		List<?> aStringListx();

		List aStringListxz();

		String[] aStringArray();

		int[] anIntArray();

		Map<String, Double> aStringDoubleMap();

		void aVoid();

		@Override
		int hashCode();
	}

	@Test
	public void testReturn() {
		Arrays.stream(TestIntf.class.getMethods()).forEachOrdered(method -> {
			Type x = method.getGenericReturnType();
			System.out.println(x +" " +method.getName() +" <"+method.getDeclaringClass().getName()+">");
		});
	}
}
