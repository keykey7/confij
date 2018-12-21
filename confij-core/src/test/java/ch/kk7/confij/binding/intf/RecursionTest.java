package ch.kk7.confij.binding.intf;

import ch.kk7.confij.binding.ConfigBinder;
import ch.kk7.confij.binding.ConfigBindingFactory;
import ch.kk7.confij.binding.ConfijDefinitionException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RecursionTest implements WithAssertions {
	public static class SoloConfigBinder extends ConfigBinder {
		public SoloConfigBinder(ConfigBindingFactory configBindingFactory) {
			super();
			getBindingFactories().clear();
			getBindingFactories().add(configBindingFactory);
		}
	}

	private ConfigBinder configBinder;

	@BeforeEach
	public void initConfigBinder() {
		configBinder = new ConfigBinder();
	}

	public <T> AbstractThrowableAssert<?, ? extends Throwable> assertThrowsRecursive(Class<T> forClass) {
		return assertThatThrownBy(() -> configBinder.toRootConfigBinding(forClass)).isInstanceOf(ConfijDefinitionException.class)
				.hasMessageContaining("circular")
				.hasMessageContaining(forClass.getSimpleName());
	}

	public interface DirectRecursion {
		DirectRecursion direct();
	}

	@Test
	public void directRecursion() {
		assertThrowsRecursive(DirectRecursion.class);
	}

	public interface FirstStep {
		SecondStep hop();
	}

	public interface SecondStep {
		FirstStep andBack();
	}

	@Test
	public void indirectRecursion() {
		assertThrowsRecursive(FirstStep.class).hasMessageContaining(SecondStep.class.getSimpleName());
	}

	public interface RecursionWithList {
		List<RecursionWithList[]> fancyButStillRecursive();
	}

	@Test
	public void otherTypesInBetween() {
		assertThrowsRecursive(RecursionWithList.class);
	}

	public interface GenericMaybeRecursive<T> {
		T get();
	}

	public interface GenericActuallyRecursive extends GenericMaybeRecursive<GenericActuallyRecursive> {
	}

	@Test
	public void genericActuallyRecursive() {
		assertThrowsRecursive(GenericActuallyRecursive.class);
	}

	public interface GenericActuallyRecursive2 extends GenericMaybeRecursive<FirstStep> {
	}

	@Test
	public void genericActuallyRecursive2() {
		assertThrowsRecursive(GenericActuallyRecursive2.class);
	}

	public interface GenericNotRecursive extends GenericMaybeRecursive<String> {
	}

	@Test
	public void genericNotRecursive() {
		configBinder.toRootConfigBinding(GenericNotRecursive.class);
	}

	public interface GenericNotRecursiveDeep extends GenericMaybeRecursive<GenericMaybeRecursive<String>> {
	}

	@Test
	public void genericNotRecursiveDeep() {
		configBinder.toRootConfigBinding(GenericNotRecursiveDeep.class);
	}
}
