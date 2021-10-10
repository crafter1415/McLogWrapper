package com.mkm75.mclw.mcdi;

public class InitializationException extends RuntimeException {
	private static final long serialVersionUID = -1L;
	public InitializationException() {
		super();
	}
	public InitializationException(String msg) {
		super(msg);
	}
	public InitializationException(Throwable e) {
		super(e);
	}
	public InitializationException(String msg, Throwable e) {
		super(msg, e);
	}
}
