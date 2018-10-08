package ch.kk7.confij.reload;

public interface ReloadStrategy {
	boolean shouldReload();

	void success();

	void failure();

	ReloadStrategy NOOP = new ReloadStrategy() {
		@Override
		public boolean shouldReload() {
			return false;
		}

		@Override
		public void success() {

		}

		@Override
		public void failure() {

		}
	};
}
