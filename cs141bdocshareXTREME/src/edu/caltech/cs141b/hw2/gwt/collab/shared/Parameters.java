package edu.caltech.cs141b.hw2.gwt.collab.shared;

public class Parameters {
	
	/**
	 * This is the length of time, in milliseconds, that each client is allowed
	 * to have the lock for.
	 */
	public static final long TIMEOUT = 1000 * 300;
	
	/**
	 * This is the maximum number of tabs that can be open in the application
	 * window at the same time.
	 */
	public static final int MAX_TABS = 8;
	
	/**
	 * This the maximum number of characters that can be in the title of a
	 * document before it is truncated. If a title is truncated, it is replaced
	 * with the first 'MAX_TITLE_CHARS - 3' characters of the title, plus 
	 * '...'.
	 */
    public static final int MAX_TITLE_CHARS = 8;
}

