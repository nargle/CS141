package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLocker implements AsyncCallback<String> {
	
	private Collaborator collaborator;
	
	public DocLocker(Collaborator collaborator) {
		this.collaborator = collaborator;
	}
	
	public void lockDocument(String key) {
		collaborator.statusUpdate("Attempting to lock document.");
		collaborator.waitingKey = key;
		collaborator.lockedDoc = new LockedDocument(null, null, 
				collaborator.readOnlyDoc.getKey(),
				collaborator.readOnlyDoc.getTitle(),
				collaborator.readOnlyDoc.getContents());
		collaborator.readOnlyDoc = null;
		collaborator.collabService.lockDocument(key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockUnavailable) {
			collaborator.statusUpdate("LockUnavailable: " + caught.getMessage());
		} else {
			collaborator.statusUpdate("Error retrieving lock"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error getting document lock.", caught);
		}
		collaborator.setDefaultButtons();
		collaborator.readOnlyDoc = new UnlockedDocument(
				collaborator.lockedDoc.getKey(),
				collaborator.lockedDoc.getTitle(),
				collaborator.lockedDoc.getContents());
		collaborator.lockedDoc = null;
	}

	@Override
	public void onSuccess(String token) {
//		if (result.getKey().equals(collaborator.waitingKey)) {
//			collaborator.statusUpdate("Lock retrieved for document.");
//			gotDoc(result);
//		} else {
//			collaborator.statusUpdate("Got lock for document which is "
//					+ "no longer active.  Releasing lock.");
//			collaborator.releaser.releaseLock(result);
//		}
		
		collaborator.channelToken = token;
	}
	
	/**
	 * Generalized so that it can be used elsewhere.  In particular, when
	 * creating a new document, a locked document is simulated by calling this
	 * function with a new LockedDocument object without the lock primitives.
	 * 
	 * @param result
	 */
	protected void gotDoc(LockedDocument result) {
		collaborator.readOnlyDoc = null;
		collaborator.lockedDoc = result;
		collaborator.setDocLockedButtons();
	}
	
}

