package ch.kk7.confij.common;

public interface ServiceLoaderPriority {
	int DEFAULT_PRIORITY = 0;

	int getPriority();

	static int priorityOf(Object o) {
		if (o instanceof ServiceLoaderPriority) {
			return ((ServiceLoaderPriority) o).getPriority();
		}
		return ServiceLoaderPriority.DEFAULT_PRIORITY;
	}
}
