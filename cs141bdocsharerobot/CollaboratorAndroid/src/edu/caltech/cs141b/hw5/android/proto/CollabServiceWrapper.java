package edu.caltech.cs141b.hw5.android.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.util.Log;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.DocumentMetaInfo;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.RequestMessage;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.RequestType;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.ResponseMessage;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.StatusType;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;

/**
 * Implements the API available from CollaboratorServiceImpl while hiding 
 * away the details of protocol buffer and HTTP connection.
 * 
 * @author aliu
 *
 */
public class CollabServiceWrapper {

	private static final String TAG = "CollabServiceWrapperAsync";

	public CollabServiceWrapper() {
	}
	
	/**
	 * Test getting a list of documents
	 * @return
	 */
	public List<DocumentMetadata> getDocumentList() {		
		Log.i(TAG, "Starting getting doucment list...");

		RequestMessage request = RequestMessage.newBuilder()
				.setRequestType(RequestType.GET_DOCUMENT_LIST)
				.build();
		
		List<DocumentMetadata> docs = new ArrayList<DocumentMetadata>();
		
		RequestTaskAsync reqTask = new RequestTaskAsync();
		reqTask.execute(request);
	
		try {
			ResponseMessage response = reqTask.get();
			if (response == null)
				return null;
			if (response.getStatusType() == StatusType.SUCCESS) {
				for (DocumentMetaInfo info : response.getDocMetaList()) {
					docs.add(DataConverter.buildDocumentMetadata(info));
				}
			}			
		} catch (InterruptedException e) {
			Log.e(TAG, "Thrad interrupted." + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "Getting response terminated from exception." + e.getMessage());
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
		
		RequestTaskAsync reqTask = new RequestTaskAsync();
		reqTask.execute(request);
				
		try {
			ResponseMessage response = reqTask.get();
			if (response == null)
				return null;
			switch (response.getStatusType()) {
				case SUCCESS:
					return DataConverter.buildLockedDocument(response.getLockedDoc());
				case LOCK_UNAVAILABLE:
					throw new LockUnavailable(response.getMessage());	
				default:
					throw new InvalidRequest(response.getMessage());
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "Thrad interrupted." + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "Getting response terminated from exception." + e.getMessage());
		}
		return null;
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
		
		RequestTaskAsync reqTask = new RequestTaskAsync();
		reqTask.execute(request);			
		
		try {
			ResponseMessage response = reqTask.get();
			if (response == null)
				return null;
			switch (response.getStatusType()) {
				case SUCCESS:
					return DataConverter.buildUnlockedDocument(response.getUnlockedDoc());
				default:
					throw new InvalidRequest(response.getMessage());
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "Thrad interrupted." + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "Getting response terminated from exception." + e.getMessage());
		}		
		return null;
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
		
		RequestTaskAsync reqTask = new RequestTaskAsync();
		reqTask.execute(request);		
		
		try {
			ResponseMessage response = reqTask.get();
			if (response == null)
				return null;
			switch (response.getStatusType()) {
				case SUCCESS:
					return DataConverter.buildUnlockedDocument(response.getUnlockedDoc());
				case LOCK_EXPIRED:
					throw new LockExpired(response.getMessage());	
				default:
					throw new InvalidRequest(response.getMessage());
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "Thrad interrupted." + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "Getting response terminated from exception." + e.getMessage());
		}		
		return null;
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
		
		RequestTaskAsync reqTask = new RequestTaskAsync();
		reqTask.execute(request);	
		
		try {
			ResponseMessage response = reqTask.get();
			if (response == null)
				return;
			switch (response.getStatusType()) {
				case SUCCESS:
					return;
				case LOCK_EXPIRED:
					throw new LockExpired(response.getMessage());	
				default:
					throw new InvalidRequest(response.getMessage());
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "Thrad interrupted." + e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, "Getting response terminated from exception." + e.getMessage());
		}
	}
}
