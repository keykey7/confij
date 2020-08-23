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
		String x1();

		String x2();
	}

	private PropertiesSource source;

	private ManualReloadStrategy<A> reload;

	private ConfijWrapper<A> wrapper;

	private A first;

	@BeforeEach
	public void init() {
		source = new PropertiesSource().set("x1", "1")
				.set("x2", "2");
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
		String newValue = new Random().nextInt() + "";
		A updated = setAndReload("x2", newValue);
		assertThat(first).as("proxy instance must change to be thread safe")
				.isNotSameAs(wrapper.get());
		assertThat(updated.x2()).isEqualTo(newValue)
				.isNotEqualTo(first.x2());
	}

	@Test
	public void customReloadHandlerTriggers() {
		String newValue = new Random().nextInt() + "";
		AtomicBoolean handlerWasCalled = new AtomicBoolean(false);
		wrapper.getReloadNotifier()
				.registerReloadHandler(event -> {
					assertThat(event.getOldValue()).isEqualTo(first.x2());
					assertThat(event.getNewValue()).isEqualTo(newValue);
					assertThat(event.getEventPath()).isEqualTo(URI.create("config:/x2"));
					assertThat(event.getChangedPaths()).containsExactly(URI.create("config:/x2"));
					handlerWasCalled.set(true);
				}, first.x2());
		assertThat(handlerWasCalled).isFalse();
		setAndReload("x2", newValue);
		//noinspection ConstantConditions
		assertThat(handlerWasCalled).isTrue();
	}

	@Test
	public void noReloadHandlerPossibleOnDuplicate() {
		wrapper = ConfijBuilder.of(A.class)
				.bindValuesForClassWith(x -> "always me", String.class)
				.buildWrapper();
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(x -> {
				}, wrapper.get()
						.x1())).isInstanceOf(ConfijException.class)
				.hasMessageContaining("always me")
				.hasMessageContaining("x1")
				.hasMessageContaining("x2");
	}

	@Test
	public void noReloadHandlerPossibleOnUnknownPrimitive() {
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(x -> {
				}, "42")).isInstanceOf(ConfijException.class)
				.hasMessageContaining("42");
	}
}
