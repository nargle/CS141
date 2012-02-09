package edu.caltech.cs141b.hw2.gwt.collab.shared;

/**
 * Thrown when a lock cannot currently be retrieved from the server for the
 * specified document.  The server should return when the lock is available
 * in the exception message.
 */
public class LockUnavailable extends Exception {

	private static final long serialVersionUID = -8039330302911776861L;
	
	public LockUnavailable() {
		
	}
	
	public LockUnavailable(String message) {
		super(message);
	}

}

