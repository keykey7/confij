package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.ConfijBuilder.ConfijWrapper;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class ConfijReloadNotifierTest implements WithAssertions {

	interface A {
		int int1();

		int int2();
	}

	private PropertiesSource source;

	private ManualReloadStrategy<A> reload;

	private ConfijWrapper<A> wrapper;

	private A first;

	@BeforeEach
	public void init() {
		source = new PropertiesSource().set("int1", "1")
				.set("int2", "2");
		reload = new ManualReloadStrategy<>();
		wrapper = ConfijBuilder.of(A.class)
				.loadFrom(source)
				.withReloadStrategy(reload)
				.buildWrapper();
		first = wrapper.get();
	}

	public A setAndReload(String key, Object value) {
		source.set(key, "" + value);
		reload.reload();
		return wrapper.get();
	}

	@Test
	public void sameSourceMeansSameConfig() {
		reload.reload();
		assertThat(first).as("proxy instance shouldn't change if there are no changes (not a hard requirement)")
				.isSameAs(wrapper.get());
	}

	@Test
	public void changedSourceMeansNewConfigInstance() {
		int newInt = new Random().nextInt();
		A updated = setAndReload("int2", newInt);
		assertThat(first).as("proxy instance must change to be thread safe")
				.isNotSameAs(wrapper.get());
		assertThat(updated.int2()).isEqualTo(newInt)
				.isNotEqualTo(first.int2());
	}

	@Test
	public void customReloadHandlerOnPrimitive() {
		int newInt = new Random().nextInt();
		AtomicBoolean handlerWasCalled = new AtomicBoolean(false);
		wrapper.getReloadNotifier()
				.registerReloadHandler(first.int2(), event -> {
					assertThat(event.getOldValue()).isEqualTo(first.int2());
					assertThat(event.getNewValue()).isEqualTo(newInt);
					assertThat(event.getEventPath()).isEqualTo(URI.create("config:/int2"));
					assertThat(event.getChangedPaths()).containsExactly(URI.create("config:/int2"));
					handlerWasCalled.set(true);
				});
		assertThat(handlerWasCalled).isFalse();
		setAndReload("int2", newInt);
		//noinspection ConstantConditions
		assertThat(handlerWasCalled).isTrue();
	}

	@Test
	public void noReloadHandlerPossibleOnDuplicatePrimitive() {
		setAndReload("int2", first.int1());
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(first.int1(), x -> {
				})).isInstanceOf(ConfijException.class)
				.hasMessageContaining("int2");

	}

	@Test
	public void noReloadHandlerPossibleOnUnknownPrimitive() {
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(42, x -> {
				})).isInstanceOf(ConfijException.class)
				.hasMessageContaining("42");

	}
}
