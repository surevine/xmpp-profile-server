package com.surevine.profileserver.db.exception;

public class DataStoreException extends Exception {
	private static final long serialVersionUID = 1L;

	public DataStoreException(final String message) {
		super(message);
	}

	public DataStoreException(final String message, final Throwable t) {
		super(message, t);
	}

	public DataStoreException(final Throwable t) {
		super(t);
	}

	public DataStoreException() {
		super();
	}
}