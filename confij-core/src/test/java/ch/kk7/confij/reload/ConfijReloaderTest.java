package ch.kk7.confij.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ConfijReloaderTest implements WithAssertions {
	private static class AwaitingPipeline implements ConfijPipeline<String> {
		private Lock lock = new ReentrantLock();

		private int counter = 0;

		@Override
		public String build() {
			lock.lock();
			try {
				return "done" + counter++;
			} finally {
				lock.unlock();
			}
		}
	}

	private static class TestReloadStrategy implements ReloadStrategy {
		private boolean doReload = false;

		@Override
		public boolean shouldReload() {
			return doReload;
		}

		@Override
		public void success() {

		}

		@Override
		public void failure() {

		}
	}

	private AwaitingPipeline pipeline;
	private TestReloadStrategy reloadStrategy;
	private ConfijReloader<String> reloader;

	@BeforeEach
	public void init() {
		pipeline = new AwaitingPipeline();
		reloadStrategy = new TestReloadStrategy();
		reloader = ConfijReloader.<String>builder().pipeline(pipeline)
				.reloadStrategy(reloadStrategy)
				.build();
	}

	@Test
	public void isLoadedOnStart() {
		assertThat(pipeline.counter).isEqualTo(1);
		assertThat(reloader.get()).isEqualTo("done0");
		assertThat(reloader.get()).isEqualTo("done0");
	}

	@Test
	public void isReloadedByStrategy() {
		reloadStrategy.doReload = true;
		assertThat(reloader.get()).isEqualTo("done1");
		assertThat(reloader.get()).isEqualTo("done2");
		reloadStrategy.doReload = false;
		assertThat(reloader.get()).isEqualTo("done2");
	}

	@Test
	public void ignoresWhenAlreadyReloading() throws InterruptedException {
		reloadStrategy.doReload = true;
		pipeline.lock.lock();
		Thread other = new Thread(() -> {
			// will hang here
			assertThat(reloader.get()).isEqualTo("done1");
		});
		other.start();
		Thread.sleep(10); // uncool, but quickest
		assertThat(reloader.get()).isEqualTo("done0");
		pipeline.lock.unlock();
		other.join();
		assertThat(reloader.get()).isEqualTo("done2");
	}
}
