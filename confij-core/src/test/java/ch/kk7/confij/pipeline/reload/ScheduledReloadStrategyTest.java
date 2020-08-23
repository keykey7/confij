package ch.kk7.confij.pipeline.reload;

import ch.kk7.confij.ConfijBuilder;
import ch.kk7.confij.common.ConfijException;
import ch.kk7.confij.common.GenericType;
import ch.kk7.confij.pipeline.ConfijPipeline;
import lombok.Data;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

class ScheduledReloadStrategyTest implements WithAssertions {
	@Data
	private static class MockPipeline implements ConfijPipeline<Integer> {
		int updatesDone = 0;

		@Override
		public Integer build() {
			return updatesDone++;
		}
	}

	@Test
	public void testScheduled() {
		MockPipeline mockPipeline = new MockPipeline();
		new ScheduledReloadStrategy<Integer>(Duration.ofSeconds(10), Duration.ofMillis(1)).register(mockPipeline);
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(mockPipeline.getUpdatesDone()).isGreaterThanOrEqualTo(1));
	}

	@Test
	public void notNull() {
		assertThatThrownBy(() -> new ScheduledReloadStrategy<>(Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ScheduledReloadStrategy<>(Duration.ofMillis(1), Duration.ZERO.minusDays(1))).isInstanceOf(
				IllegalArgumentException.class);
	}

	@Test
	public void builderWithoutWrapperMakesNoSense() {
		assertThatThrownBy(() -> ConfijBuilder.of(new GenericType<Integer>() {
		})
				.reloadStrategy(new ScheduledReloadStrategy<>())
				.build()).isInstanceOf(ConfijException.class)
				.hasMessageContaining("buildWrapper");
	}
}
