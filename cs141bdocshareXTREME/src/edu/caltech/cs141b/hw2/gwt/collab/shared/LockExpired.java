package edu.caltech.cs141b.hw2.gwt.collab.shared;

/**
 * Thrown when the provided lock objects cannot be used any longer.
 */
public class LockExpired extends Exception {

	private static final long serialVersionUID = 7796506690276524937L;
	
	public LockExpired() {
		
	}

	public LockExpired(String message) {
		super(message);
	}

}

