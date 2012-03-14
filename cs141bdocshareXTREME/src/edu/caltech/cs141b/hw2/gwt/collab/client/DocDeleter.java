package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Used in conjunction with <code>CollaboratorService.getDocument()</code>.
 */
public class DocDeleter implements AsyncCallback<Void> {

	private Collaborator collaborator;

	public DocDeleter(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void deleteDocument(String key) {
		collaborator.statusUpdate("Deleting document " + key + ".");
		collaborator.waitingKey = key;
		collaborator.collabService.deleteDocument(key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error deleting document"
				+ "; caught exception " + caught.getClass()
				+ " with message: " + caught.getMessage());
		GWT.log("Error getting document lock.", caught);
	}

	@Override
	public void onSuccess(Void v) {
		collaborator.statusUpdate("Document deleted.");
	}

}