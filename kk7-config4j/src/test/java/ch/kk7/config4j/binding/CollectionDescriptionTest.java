package ch.kk7.config4j.binding;

import ch.kk7.config4j.binding.collection.CollectionBinding;
import ch.kk7.config4j.binding.collection.CollectionBindingFactory;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionDescriptionTest {

	private TypeResolver typeResolver = new TypeResolver();

	public interface Xxx {
		Set<Integer> integerSet();
		Set<Set<Long>> longSetSet();

		Set<?> wildcardSet();
		Set rawSet();
	}

	private static CollectionBindingFactory collectionFactory;

	@BeforeAll
	public static void initSetFactory() {
		collectionFactory = new CollectionBindingFactory();
	}

	@ParameterizedTest
	@ValueSource(strings = {"integerSet", "longSetSet"})
	public void testValidSets(String methodName) throws Exception {
		ResolvedType type = typeResolver.resolve(Xxx.class.getMethod(methodName)
				.getGenericReturnType());
		Optional<CollectionBinding> optional = collectionFactory.maybeCreate(type, new ConfigBinder());
		assertTrue(optional.isPresent());
	}

	public class MySet<T> extends HashSet<T> {

	}

	public class XXXXX  {
		public HashSet<Set<Integer>> elo() {
			return null;
		}
	}

	@Test
	public void asd() throws Exception {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType rtype = typeResolver.resolve(XXXXX.class.getMethod("elo")
				.getGenericReturnType());
		List<ResolvedType> resolvedTypeList = rtype.typeParametersFor(Collection.class);
		assertTrue(resolvedTypeList.get(0)
				.isInstanceOf(Collection.class));
		assertEquals(Integer.class, resolvedTypeList.get(0)
				.typeParametersFor(Collection.class).get(0).getErasedType());

	}

//	@Test
//	public void tttxt() throws Exception {
//		Type x = XXXXX.class.getMethod("elo")
//				.getGenericReturnType();
//		Class<?>[] typeArgs = TypeResolver.resolveRawClass(x, XXXXX.class);
//		assertEquals(1, typeArgs.length);
//		assertEquals(Integer.class, typeArgs[0]);
//	}
}
