package edu.caltech.cs141b.hw5.android.data;

/**
 * Thrown when the request message is not properly filled out.
 */
public class InvalidRequest extends Exception {

	private static final long serialVersionUID = 6874113728117829393L;

	public InvalidRequest() {
		
	}
	
	public InvalidRequest(String message) {
		super(message);
	}
}
