package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.ConfijBuilder.ConfijWrapper;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.common.GenericType;
import ch.kk7.confij.source.env.PropertiesSource;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ReloadNotifierImplTest implements WithAssertions {
	interface A {
		String x1();

		String x2();

		B b();
	}

	interface B {
		int x3();
	}

	private PropertiesSource source;
	private ManualReloadStrategy reload;
	private ConfijWrapper<A> wrapper;
	private A first;

	@BeforeEach
	void init() {
		source = new PropertiesSource().set("x1", "1")
				.set("x2", "2")
				.set("b.x3", "3");
		reload = new ManualReloadStrategy();
		wrapper = ConfijBuilder.of(A.class)
				.loadFrom(source)
				.reloadStrategy(reload)
				.buildWrapper();
		first = wrapper.get();
	}

	public A setAndReload(String key, Object value) {
		source.set(key, "" + value);
		reload.reload();
		return wrapper.get();
	}

	@Test
	void sameSourceMeansSameConfig() {
		reload.reload();
		assertThat(first).as("proxy instance shouldn't change if there are no changes (not a hard requirement)")
				.isSameAs(wrapper.get());
	}

	@Test
	void changedSourceMeansNewConfigInstance() {
		String newValue = new Random().nextInt() + "";
		A updated = setAndReload("x2", newValue);
		assertThat(first).as("proxy instance must change to be thread safe")
				.isNotSameAs(wrapper.get());
		assertThat(updated.x2()).isEqualTo(newValue)
				.isNotEqualTo(first.x2());
	}

	@Test
	void customReloadHandlerTriggers() {
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
	void noReloadHandlerOnPrimitiveAllowed() {
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(x -> {
				}, first.b()
						.x3())).isInstanceOf(ConfijException.class)
				.hasMessageContaining("primitive");
	}

	@Test
	void handlerOnChildPath() {
		AtomicBoolean handlerWasCalled = new AtomicBoolean(false);
		wrapper.getReloadNotifier()
				.registerReloadHandler(event -> {
					assertThat(event.getOldValue()).isEqualTo(3);
					assertThat(event.getNewValue()).isEqualTo(1337);
					handlerWasCalled.set(true);
				}, first.b(), "x3");
		assertThat(handlerWasCalled).isFalse();
		setAndReload("b.x3", 1337);
		//noinspection ConstantConditions
		assertThat(handlerWasCalled).isTrue();
	}

	@Test
	void unknownChildPath() {
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(x -> {
				}, first.b(), "notAChild")).isInstanceOf(ConfijException.class)
				.hasMessageContaining("notAChild");
	}

	@Test
	void noReloadHandlerPossibleOnDuplicate() {
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
	void noReloadHandlerPossibleOnUnknownValue() {
		assertThatThrownBy(() -> wrapper.getReloadNotifier()
				.registerReloadHandler(x -> {
				}, "42")).isInstanceOf(ConfijException.class)
				.hasMessageContaining("42");
	}

	@Test
	void atomicInstance() {
		AtomicReference<A> reference = wrapper.getReloadNotifier()
				.registerAtomicReference(first);
		assertThat(first).isSameAs(reference.get());
		A updated = setAndReload("x2", "whatever");
		assertThat(updated).isSameAs(reference.get());
	}

	@Test
	void listModifications() {
		PropertiesSource src = new PropertiesSource().set("0", "first");
		reload = new ManualReloadStrategy();
		ConfijWrapper<List<String>> listWrapper = ConfijBuilder.of(new GenericType<List<String>>() {
		})
				.loadFrom(src)
				.reloadStrategy(reload)
				.buildWrapper();
		assertThat(listWrapper.get()).containsExactly("first");

		src.set("1", "second");
		reload.reload();
		assertThat(listWrapper.get()).containsExactly("first", "second");

		src.set("0", null);
		src.set("1", null);
		reload.reload();
		assertThat(listWrapper.get()).isEmpty();
	}
}
