package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TabBar;

import edu.caltech.cs141b.hw2.gwt.collab.shared.Parameters;
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
		collaborator.loadDoc.setEnabled(true);
	}

	@Override
	public void onSuccess(UnlockedDocument result) {
		if (result.getKey().equals(collaborator.waitingKey)) {
			collaborator.statusUpdate("Document '" + result.getTitle()
					+ "' successfully retrieved.");
			if (collaborator.isReload) {
				//If reloading a document that is already open
				TabBar tabs = collaborator.openTabs.getTabBar();
				//tabs.setTabText(collaborator.currentTab, result.getTitle());
				if(result.getTitle().length() > Parameters.MAX_TITLE_CHARS)
					tabs.setTabText(collaborator.currentTab,
							result.getTitle().substring(0, 5) + "...");
				else
					tabs.setTabText(collaborator.currentTab, result.getTitle());
				collaborator.title.setValue(result.getTitle());
				collaborator.contents.setHTML(result.getContents());
			}
			else
				collaborator.openDoc(result.getKey(), result.getTitle(), result.getContents());
			gotDoc(result);
		} else {
			collaborator.statusUpdate("Returned document that is no longer "
					+ "expected; discarding.");
		}
		collaborator.loadDoc.setEnabled(true);
	}
	
	/**
	 * Generalized so that it can be called elsewhere.  In particular, after
	 * a document is saved, it calls this function to simulate an initial
	 * reading of a document.
	 * 
	 * @param result the unlocked document that should be displayed
	 */
	protected void gotDoc(UnlockedDocument result) {
		collaborator.lockedDoc = null;
		collaborator.readOnlyDoc = result;
		collaborator.setDefaultButtons();
		collaborator.isReload = false;
		History.newItem(result.getKey());
	}
}

