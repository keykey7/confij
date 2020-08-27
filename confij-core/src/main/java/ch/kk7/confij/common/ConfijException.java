package ch.kk7.confij.common;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Base64;

import static ch.kk7.confij.logging.LogUtil.formatLog;
import static ch.kk7.confij.logging.LogUtil.throwableOrNull;

public class ConfijException extends RuntimeException {
	/**
	 * a unique code for this kind of exception
	 */
	@Getter
	private final String code;

	public ConfijException(String s) {
		super(s);
		code = toCode(s);
	}

	public ConfijException(String s, Throwable throwable) {
		super(s, throwable);
		code = toCode(s);
	}

	public ConfijException(String s, Object... args) {
		super(formatLog(s, args), throwableOrNull(args));
		code = toCode(s);
	}

	protected static String toCode(String s) {
		return Base64.getEncoder()
				.encodeToString(ByteBuffer.allocate(4)
						.putInt(s.hashCode())
						.array());
	}
}
