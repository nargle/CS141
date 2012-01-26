package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Wrapper class for UnlockedDocuments and LockedDocuments, with all the methods
 * available to either. Throws an Exception if a method is invoked while 'doc'
 * is something other than an UnlockedDocument or LockedDocument, or if a
 * method found only in LockedDocument is called while 'doc' is an
 * UnlockedDocument. It also contains the following two methods:
 *     - unlock(): Unlike the version in LockedDocument, this does not return
 *                 an UnlockedDocument and instead returns nothing. It sets
 *                 'doc' to the UnlockedDocument returned by 'doc.unlock()'.
 *     - lock(): Locks an UnlockedDocument, given values for 'lockedBy' and
 *               'lockedUntil'.
 *     - isLocked(): Returns 'true' if 'doc' is a LockedDocument and 'false' if
 *                   it is an UnlockedDocument.
 *     - getDocumentMetaData(): Returns the corresponding DocumentMetadata for
 *                              'doc'.
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
	
	public void lock(String lockedBy, Date lockedUntil) throws Exception {
		if(doc instanceof UnlockedDocument)
			doc = new LockedDocument(lockedBy, lockedUntil, this.getKey(), 
					this.getTitle(), this.getContents());
		else if(doc instanceof LockedDocument)
			throw new Exception("Document: 'doc' is LockedDocument, so " +
					"cannot invoke 'lock()'.");
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


