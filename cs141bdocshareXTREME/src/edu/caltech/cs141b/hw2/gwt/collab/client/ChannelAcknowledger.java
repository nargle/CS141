package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Used in conjunction with <code>CollaboratorService.getDocument()</code>.
 */
public class ChannelAcknowledger implements AsyncCallback<Void> {

	private Collaborator collaborator;

	public ChannelAcknowledger(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void acknowledgeChannel(String docKey) {
		collaborator.collabService.acknowledgeChannel(docKey, 
		        collaborator.clientID, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error acknowledging channel creation"
				+ "; caught exception " + caught.getClass()
				+ " with message: " + caught.getMessage());
		GWT.log("Error acknowledging channel creation.", caught);
	}

	@Override
	public void onSuccess(Void v) {
		collaborator.statusUpdate("Channel creation acknowledged; waiting for" +
				" lock to be granted.");
	}

}