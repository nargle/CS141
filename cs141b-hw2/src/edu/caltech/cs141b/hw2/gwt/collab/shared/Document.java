package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Used to contain the entire document along with the locking primitives
 * necessary to modify it.
 */
@PersistenceCapable
public class Document implements IsSerializable {

	private IsSerializable doc;
	
	public Document(UnlockedDocument doc) {
		this.doc = doc;
	}

	public String getKey() throws Exception {
		if(doc instanceof UnlockedDocument)
			return ((UnlockedDocument)doc).getKey();
		else if(doc instanceof LockedDocument)
			return ((LockedDocument)doc).getKey();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}

	public String getTitle() throws Exception {
		if(doc instanceof UnlockedDocument)
			return ((UnlockedDocument)doc).getTitle();
		else if(doc instanceof LockedDocument)
			return ((LockedDocument)doc).getTitle();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}
	
	public void setTitle(String documentTitle) throws Exception {
		if(doc instanceof UnlockedDocument)
			throw new Exception("Document: 'doc' is UnlockedDocument, so " +
					"cannot invoke 'setTitle()'.");
		else if(doc instanceof LockedDocument)
			((LockedDocument)doc).setTitle(documentTitle);
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}

	public String getContents() throws Exception {
		if(doc instanceof UnlockedDocument)
			return ((UnlockedDocument)doc).getContents();
		else if(doc instanceof LockedDocument)
			return ((LockedDocument)doc).getContents();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}

	public void setContents(String documentContents) throws Exception {
		if(doc instanceof UnlockedDocument)
			throw new Exception("Document: 'doc' is UnlockedDocument, so " +
					"cannot invoke 'setContents()'.");
		else if(doc instanceof LockedDocument)
			((LockedDocument)doc).setContents(documentContents);
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}

	public String getLockedBy() throws Exception {
		if(doc instanceof UnlockedDocument)
			throw new Exception("Document: 'doc' is UnlockedDocument, so " +
					"cannot invoke 'getLockedBy()'.");
		else if(doc instanceof LockedDocument)
			return ((LockedDocument)doc).getLockedBy();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}

	public Date getLockedUntil() throws Exception {
		if(doc instanceof UnlockedDocument)
			throw new Exception("Document: 'doc' is UnlockedDocument, so " +
					"cannot invoke 'getLockedUntil()'.");
		else if(doc instanceof LockedDocument)
			return ((LockedDocument)doc).getLockedUntil();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}
	
	public void unlock() throws Exception {
		if(doc instanceof UnlockedDocument)
			throw new Exception("Document: 'doc' is UnlockedDocument, so " +
					"cannot invoke 'unlock()'.");
		else if(doc instanceof LockedDocument)
			doc = ((LockedDocument) doc).unlock();
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}
	
	public boolean isLocked() throws Exception {
		if(doc instanceof UnlockedDocument)
			return false;
		else if(doc instanceof LockedDocument)
			return true;
		else
			throw new Exception("Document: 'doc' does not point to an " +
					"UnlockedDocument or LockedDocument.");
	}
	
	public DocumentMetadata getDocumentMetaData() throws Exception {
		return new DocumentMetadata(this.getKey(), this.getTitle());
	}
	
}


