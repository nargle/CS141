package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * The async counterpart of <code>CollaboratorService</code>.
 */
public interface CollaboratorServiceAsync {

	void getDocumentList(AsyncCallback<List<DocumentMetadata>> callback);

	void lockDocument(String documentKey, String clientID, 
			AsyncCallback<String> callback);

	void getDocument(String documentKey,
			AsyncCallback<UnlockedDocument> callback);

	void saveDocument(LockedDocument doc, String clientID, 
			AsyncCallback<UnlockedDocument> callback);

	void releaseLock(LockedDocument doc, String clientID, 
			AsyncCallback<UnlockedDocument> callback);
	
	void deleteDocument(String documentKey, String clientID, 
			AsyncCallback<Void> callback);
	
	void acknowledgeChannel(String docKey, String clientID, 
			AsyncCallback<Void> callback);
	
	void cleanup(String docKey, AsyncCallback<Void> callback);
	
	void getID(AsyncCallback<String> id);
}

