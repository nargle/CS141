package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CleanupReminder extends Timer {

	private Collaborator collaborator;
	private String docKey;

	public CleanupReminder(Collaborator collaborator, String docKey)
	{
		this.collaborator = collaborator;
		this.docKey = docKey;
	}

	public void run()
	{
		collaborator.collabService.cleanup(docKey, 
				new AsyncCallback<Void>()
				{
					public void onFailure(Throwable caught) {}
					
					public void onSuccess(Void v) {}
				});
	}
}