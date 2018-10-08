package ch.kk7.confij.reload;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.time.Duration;
import java.time.Instant;

@Value
@Builder
public class TimeIntervalReloadStrategy implements ReloadStrategy {
	@Builder.Default
	private final Duration pauseAfterSuccess = Duration.ofSeconds(10);
	@Builder.Default
	private final Duration pauseAfterFailure = Duration.ofSeconds(10);
	@NonFinal
	@Builder.Default
	private Instant nextReloadAt = Instant.MIN;

	@Override
	public boolean shouldReload() {
		return nextReloadAt.isBefore(Instant.now());
	}

	@Override
	public void success() {
		nextReloadAt = Instant.now()
				.plus(pauseAfterSuccess);
	}

	@Override
	public void failure() {
		nextReloadAt = Instant.now()
				.plus(pauseAfterFailure);
	}
}
