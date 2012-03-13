package edu.caltech.cs141b.hw5.proto;

import java.util.ArrayList;
import java.util.List;

import edu.caltech.cs141b.hw5.data.CollabMessages.DocumentMetaInfo;
import edu.caltech.cs141b.hw5.data.CollabMessages.RequestMessage;
import edu.caltech.cs141b.hw5.data.CollabMessages.RequestType;
import edu.caltech.cs141b.hw5.data.CollabMessages.ResponseMessage;
import edu.caltech.cs141b.hw5.data.CollabMessages.StatusType;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DataConverter;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw5.gwt.collab.shared.InvalidRequest;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw5.gwt.collab.shared.UnlockedDocument;

/**
 * Wrapper class for doing all the function calls to Collaborator
 * 
 * @author aliu
 *
 */
public class CollaboratorServiceWrapper {

	// Replace hostname with the deployed App Engine address
	String url = "http://localhost:8888/collab/protoc";
	
	ProtoBufConnector conn;
	
	public CollaboratorServiceWrapper() {
		conn = new ProtoBufConnector(url);
	}
	
	/**
	 * Test getting a list of documents
	 * @return
	 */
	public List<DocumentMetadata> getDocumentList() {
		
		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.GET_DOCUMENT_LIST)
				.build();
		
		ResponseMessage response = conn.makePostConnect(request);
		
		List<DocumentMetadata> docs = new ArrayList<DocumentMetadata>();
		if (response.getStatusType() == StatusType.SUCCESS) {
			for (DocumentMetaInfo info : response.getDocMetaList()) {
				docs.add(DataConverter.buildDocumentMetadata(info));
			}
		}
		return docs;
	}
	
	/**
	 * Lock a document by key value.
	 * 
	 * @param key
	 * @return locked document
	 * @throws LockUnavailable 
	 * @throws InvalidRequest 
	 */
	public LockedDocument lockDocument(String key) throws LockUnavailable, InvalidRequest {
		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.LOCK_DOCUMENT)
				.setDocumentKey(key)
				.build();
		
		ResponseMessage response = conn.makePostConnect(request);
		
		switch (response.getStatusType()) {
			case SUCCESS:
				return DataConverter.buildLockedDocument(response.getLockedDoc());
			case LOCK_UNAVAILABLE:
				throw new LockUnavailable(response.getMessage());	
			default:
				throw new InvalidRequest(response.getMessage());
		}
	}
	
	/**
	 * Get a document by key
	 * @param key
	 * @return read-only document
	 * @throws InvalidRequest
	 */
	public UnlockedDocument getDocument(String key) throws InvalidRequest {
		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.GET_DOCUMENT)
				.setDocumentKey(key)
				.build();
		
		ResponseMessage response = conn.makePostConnect(request);
		
		switch (response.getStatusType()) {
			case SUCCESS:
				return DataConverter.buildUnlockedDocument(response.getUnlockedDoc());
			default:
				throw new InvalidRequest(response.getMessage());
		}
	}
	
	/**
	 * Save a locked document
	 * @param doc
	 * @return unlocked document
	 * @throws LockExpired
	 * @throws InvalidRequest
	 */
	public UnlockedDocument saveDocument(LockedDocument doc) 
			throws LockExpired, InvalidRequest {
		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.SAVE_DOCUMENT)
				.setLockedDoc(DataConverter.buildLockedDocumentInfo(doc))
				.build();
		
		ResponseMessage response = conn.makePostConnect(request);
		
		switch (response.getStatusType()) {
			case SUCCESS:
				return DataConverter.buildUnlockedDocument(response.getUnlockedDoc());
			case LOCK_EXPIRED:
				throw new LockExpired(response.getMessage());	
			default:
				throw new InvalidRequest(response.getMessage());
		}
	}
	
	/**
	 * Release lock of a locked document
	 * @param doc
	 * @throws LockExpired
	 * @throws InvalidRequest
	 */
	public void releaseLock(LockedDocument doc) throws LockExpired, InvalidRequest {
		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.RELEASE_LOCK)
				.setLockedDoc(DataConverter.buildLockedDocumentInfo(doc))
				.build();
		
		ResponseMessage response = conn.makePostConnect(request);
		
		switch (response.getStatusType()) {
			case SUCCESS:
				return;
			case LOCK_EXPIRED:
				throw new LockExpired(response.getMessage());	
			default:
				throw new InvalidRequest(response.getMessage());
		}
	}
	
	
	public static void main(String[] args) {
		CollaboratorServiceWrapper tester = new CollaboratorServiceWrapper();
		
		// Test get document list
		List<DocumentMetadata> docs = tester.getDocumentList();
		for (DocumentMetadata doc : docs) {
			System.out.println(doc.getKey() + ": " + doc.getTitle());
		}
		
		// Test lock the first document in the list for dt seconds then release it
		if (!docs.isEmpty()) {
			String key = docs.get(0).getKey();
			try {
			LockedDocument ld = tester.lockDocument(key);
			System.out.println(ld.getTitle() + ":" + ld.getLockedUntil() + ":" 
					+ ld.getLockedBy() + "\n"
					+ ld.getContents());
					
			// Wait for 10 seconds
			Thread.sleep(10000);
			
			// release the lock
			tester.releaseLock(ld);
			
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}
	}
}
