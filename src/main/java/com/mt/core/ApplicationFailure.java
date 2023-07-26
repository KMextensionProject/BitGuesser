package com.mt.core;

public class ApplicationFailure extends RuntimeException {

	private static final long serialVersionUID = -7269321452077536859L;

	public ApplicationFailure(String message) {
		super(message);
	}

	public ApplicationFailure(String message, Throwable cause) {
		super(message, cause);
	}
}
