package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.getDocument()</code>.
 */
public class DocReader implements AsyncCallback<UnlockedDocument> {
	
	private Collaborator collaborator;
	
	public DocReader(Collaborator collaborator) {
		this.collaborator = collaborator;
	}
	
	public void getDocument(String key) {
		collaborator.statusUpdate("Fetching document " + key + ".");
		collaborator.waitingKey = key;
		collaborator.collabService.getDocument(key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error retrieving document"
				+ "; caught exception " + caught.getClass()
				+ " with message: " + caught.getMessage());
		GWT.log("Error getting document lock.", caught);
	}

	@Override
	public void onSuccess(UnlockedDocument result) {
		if (result.getKey().equals(collaborator.waitingKey)) {
			collaborator.statusUpdate("Document '" + result.getTitle()
					+ "' successfully retrieved.");
			gotDoc(result);
		} else {
			collaborator.statusUpdate("Returned document that is no longer "
					+ "expected; discarding.");
		}
	}
	
	/**
	 * Generalized so that it can be called elsewhere.  In particular, after
	 * a document is saved, it calls this function to simulate an initial
	 * reading of a document.
	 * 
	 * @param result the unlocked document that should be displayed
	 */
	protected void gotDoc(UnlockedDocument result) {
		collaborator.readOnlyDoc = result;
		collaborator.lockedDoc = null;
		collaborator.title.setValue(result.getTitle());
		collaborator.contents.setHTML(result.getContents());
		collaborator.setDefaultButtons();
		History.newItem(result.getKey());
	}
}

