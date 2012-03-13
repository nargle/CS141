package edu.caltech.cs141b.hw5.gwt.collab.shared;

public class Parameters {
	
	/**
	 * This is the length of time, in milliseconds, that each client is allowed
	 * to have the lock for.
	 */
	public static final int TIMEOUT = 1000 * 300;
	
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
    
    /**
     * This is the maximum number of times the Collaborator will check to see if
     * its 'docToken' field has been updated after it attempts to acquire the
     * lock for a document. The first attempt is made after 1 second, the second
     * after 2 seconds, the third after 4 seconds, and so on.
     */
    public static final int MAX_CHANNEL_TRIES = 5;
    
    /**
     * This is the minimum number of milliseconds that the thinking phase of the
     * simulation lasts.
     */
    public static final int MIN_THINK_TIME = 10000;
    
    /**
     * This is the maximum number of milliseconds that the thinking phase of the
     * simulation lasts.
     */
    public static final int MAX_THINK_TIME = 10000;
    
    /**
     * This is the minimum number of milliseconds that the eating phase of the
     * simulation lasts.
     */
    public static final int MIN_EAT_TIME = 10000;
    
    /**
     * This is the maximum number of milliseconds that the eating phase of the
     * simulation lasts.
     */
    public static final int MAX_EAT_TIME = 10000;
}

