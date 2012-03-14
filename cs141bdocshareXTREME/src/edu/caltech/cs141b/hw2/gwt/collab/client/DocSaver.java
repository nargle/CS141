package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TabBar;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Parameters;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

public class DocSaver implements AsyncCallback<UnlockedDocument> {

	private Collaborator collaborator;

	public DocSaver(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void saveDocument(LockedDocument lockedDoc) {
		collaborator.statusUpdate("Attemping to save document.");
		collaborator.waitingKey = lockedDoc.getKey();
		collaborator.collabService.saveDocument(lockedDoc, this);
		collaborator.saveButton.setEnabled(false);
		collaborator.cancelButton.setEnabled(false);
		collaborator.closeButton.setEnabled(false);
		collaborator.deleteButton.setEnabled(false);
		collaborator.title.setEnabled(false);
		collaborator.contents.setEnabled(false);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockExpired) {
			collaborator.statusUpdate("Save failed: " + caught.getMessage());
		} else {
			collaborator.statusUpdate("Error saving document"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error saving document.", caught);
			collaborator.releaser.releaseLock(collaborator.lockedDoc);
		}
		if (collaborator.lockedDoc != null) {
			collaborator.reader.gotDoc(collaborator.lockedDoc.unlock());
		}

		if(collaborator.currentChannelCreator.expirationTimer != null)
			collaborator.currentChannelCreator.expirationTimer.cancel();
		collaborator.currentChannelCreator = null;
		
		// Re-enable all the tabs that were disabled when the document started
		// the saving process.
		TabBar tabs = collaborator.openTabs.getTabBar();
		for(int i = 0; i < tabs.getTabCount(); i++)
			tabs.setTabEnabled(i, true);
	}

	@Override
	public void onSuccess(UnlockedDocument result) {
		collaborator.statusUpdate("Document '" + result.getTitle()
				+ "' successfully saved.");
		if (collaborator.waitingKey == null || 
				result.getKey().equals(collaborator.waitingKey)) {
			collaborator.reader.gotDoc(result);

			// Refresh list in case title was changed.
			collaborator.lister.getDocumentList();

			// Update list of keys of all open documents.
			collaborator.keyList.set(collaborator.currentTab, result.getKey());
		} else {
			GWT.log("Saved document is not the anticipated document.");
		}
		
		if(collaborator.currentChannelCreator.expirationTimer != null)
			collaborator.currentChannelCreator.expirationTimer.cancel();
		collaborator.currentChannelCreator = null;

		// Re-enable all the tabs that were disabled when the document started
		// the saving process.
		TabBar tabs = collaborator.openTabs.getTabBar();
		for(int i = 0; i < tabs.getTabCount(); i++)
			tabs.setTabEnabled(i, true);

		// Change the tab title to the new title of the document.
		if(result.getTitle().length() > Parameters.MAX_TITLE_CHARS)
			tabs.setTabText(collaborator.currentTab,
					result.getTitle().substring(0, 
							Parameters.MAX_TITLE_CHARS - 3) + "...");
		else
			tabs.setTabText(collaborator.currentTab, result.getTitle());
	}

}

