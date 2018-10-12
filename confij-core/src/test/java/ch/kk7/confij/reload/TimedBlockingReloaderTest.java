package ch.kk7.confij.reload;

import ch.kk7.confij.pipeline.ConfijPipeline;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class TimedBlockingReloaderTest implements WithAssertions {

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

	private AwaitingPipeline pipeline;
	private TimedBlockingReloader<String> reloader;
	private final Duration reloadAfter = Duration.ofSeconds(1);

	@BeforeEach
	public void init() {
		pipeline = new AwaitingPipeline();
		reloader = new TimedBlockingReloader<>(reloadAfter, reloadAfter);
		reloader.initialize(pipeline);
	}

	@Test
	public void cannotInitializeTwice() {
		assertThrows(IllegalStateException.class, () -> reloader.initialize(pipeline));
	}

	@Test
	public void isLoadedOnStart() {
		assertThat(pipeline.counter).isEqualTo(1);
		assertThat(reloader.get()).isEqualTo("done0");
		assertThat(reloader.get()).isEqualTo("done0");
	}

	@Test
	public void isReloadedByStrategy() throws InterruptedException {
		Thread.sleep(reloadAfter.toMillis() + 10);
		assertThat(reloader.get()).isEqualTo("done1");
		assertThat(reloader.get()).isEqualTo("done1");
	}

	@Test
	public void ignoresWhenAlreadyReloading() throws InterruptedException {
		Thread.sleep(reloadAfter.toMillis() + 10);
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
		assertThat(reloader.get()).isEqualTo("done1");
	}
}
