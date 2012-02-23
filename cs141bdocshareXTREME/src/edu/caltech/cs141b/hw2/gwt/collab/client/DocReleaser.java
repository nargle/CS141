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
	
	public void releaseLock(LockedDocument lockedDoc, String token) {
		  collaborator.statusUpdate("Releasing lock on '" + lockedDoc.getTitle()
		    + "'.");
		  collaborator.saveButton.setEnabled(false);
		  collaborator.cancelButton.setEnabled(false);
		  collaborator.refreshButton.setEnabled(false);
		  collaborator.closeButton.setEnabled(false);
		  collaborator.deleteButton.setEnabled(false);
		  collaborator.collabService.releaseLock(lockedDoc, token, this);
		 }

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockExpired) {
			collaborator.statusUpdate("Lock had already expired; release failed.");
		} else {
			if (!collaborator.isDeleting) {
			    collaborator.statusUpdate("Error releasing document"
	                    + "; caught exception " + caught.getClass()
	                    + " with message: " + caught.getMessage());
	            GWT.log("Error releasing document.", caught);			          
			}
		}
		collaborator.setDefaultButtons();
		collaborator.isDeleting = false;
	}

	@Override
	public void onSuccess(Void result) {
		collaborator.setDefaultButtons();
		collaborator.statusUpdate("Document lock released.");
		collaborator.lockedDoc = null;
		if(collaborator.isCancel)
		{
            collaborator.isReload = true;
            collaborator.isCancel = false;
		}
        collaborator.isDeleting = false;
        collaborator.docToken = null;
		/*
		Unnecessary:
		else {
		    collaborator.readOnlyDoc = new UnlockedDocument(collaborator.openDocKeys.get(collaborator.currentTab), 
				collaborator.titleList.get(collaborator.currentTab).getValue(), 
				collaborator.contentsList.get(collaborator.currentTab).getHTML());
		}*/	
	}
	
}

