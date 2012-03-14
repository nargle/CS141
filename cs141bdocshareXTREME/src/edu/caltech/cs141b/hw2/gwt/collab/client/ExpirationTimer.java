package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.Timer;



public class ExpirationTimer extends Timer {
	
	private String docKey;
	private Collaborator collaborator;
	public ExpirationTimer(Collaborator collaborator, String docKey)
	{
	    this.collaborator = collaborator;
		this.docKey = docKey;
	}
	
	public void run()
	{
	    if (collaborator.lockedDoc != null && docKey.equals(collaborator.lockedDoc.getKey())) {
	        collaborator.isCancel = true;
	        collaborator.releaser.releaseLock(collaborator.lockedDoc);
	        collaborator.statusUpdate("Document lock " +
	                "EXPIRED.");
		
	        // Ensures this Timer will not run again.
	        cancel();
	    }
	}

}

