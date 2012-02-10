package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.releaseLock()</code>.
 */
public class DocReleaser implements AsyncCallback<Void> {
	
	private Collaborator collaborator;
	
	public DocReleaser(Collaborator collaborator) {
		this.collaborator = collaborator;
	}
	
	public void releaseLock(LockedDocument lockedDoc) {
		collaborator.statusUpdate("Releasing lock on '" + lockedDoc.getTitle()
				+ "'.");
		collaborator.saveButton.setEnabled(false);
		collaborator.cancelButton.setEnabled(false);
		collaborator.refreshDoc.setEnabled(false);
		collaborator.closeButton.setEnabled(false);
		collaborator.collabService.releaseLock(lockedDoc, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockExpired) {
			collaborator.statusUpdate("Lock had already expired; release failed.");
		} else {
			collaborator.statusUpdate("Error releasing document"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error releasing document.", caught);
		}
		collaborator.setDefaultButtons();
	}

	@Override
	public void onSuccess(Void result) {
		collaborator.setDefaultButtons();
		collaborator.statusUpdate("Document lock released.");
	}
	
}

