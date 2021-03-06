package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentException;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Wrapper class for UnlockedDocuments and LockedDocuments, with all the methods
 * available to either. Throws an DocumentException if a method is invoked while 'doc'
 * is something other than an UnlockedDocument or LockedDocument, or if a
 * method found only in LockedDocument is called while 'doc' is an
 * UnlockedDocument. It also contains the following three methods:
 *     - lock(): Locks an UnlockedDocument, given values for 'lockedBy' and
 *               'lockedUntil'. Returns the new LockedDocument.
 *     - isLocked(): Returns 'true' if 'doc' is a LockedDocument and 'false' if
 *                   it is an UnlockedDocument.
 *     - getDocumentMetaData(): Returns the corresponding DocumentMetadata for
 *                              'doc'.
 */
@PersistenceCapable
public class Document implements IsSerializable{

	@PrimaryKey
	@Persistent
	private String key;
	@Persistent
    private String lockedBy;
    @Persistent
    private Date lockedUntil;
    @Persistent
    private String title;
    @Persistent
    private String contents;
	
	/**
	 * Initialize 'doc' to an UnlockedDocument object.
	 * 
	 * @param doc the UnlockedDocument
	 */
	public Document(String key, String title, String contents) {
		this.key = key;
		this.lockedBy = null;
        this.lockedUntil = null;
        this.title = title;
        this.contents = contents;
	}
	
	/**
	 * Used to access the key for the current 'doc' object.
	 * 
	 * @return the key for 'doc', which is a String
	 * @throws DocumentException if 'doc' is not a valid document type
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Used to access the title for the current 'doc' object.
	 * 
	 * @return the title for 'doc', which is a String
	 * @throws DocumentException if 'doc' is not a valid document type
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Used to set the title for the current 'doc' object.
	 * 
	 * @param documentTitle, the new title of the 'doc' object
	 * @throws DocumentException if 'doc' is not a LockedDocument or if it
	 *         is not a valid document type
	 */
	public void setTitle(String documentTitle) throws DocumentException {
		if(!isLocked())
			throw new DocumentException("Document: Document is unlocked, so " +
					"cannot invoke 'setTitle()'.");
		else
			title = documentTitle;
	}

	/**
	 * Used to access the contents of the current 'doc' object.
	 * 
	 * @return the contents of the 'doc' object
	 * @throws DocumentException if 'doc' is not a valid document type
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Used to set the contents of the current 'doc' object.
	 * 
	 * @param documentContents, the new contents of 'doc'
	 * @throws DocumentException if 'doc' is not a valid document type
	 */
	public void setContents(String documentContents) throws DocumentException {
		if(!isLocked())
			throw new DocumentException("Document: Document is unlocked, so " +
					"cannot invoke 'setContents()'.");
		else 
			contents = documentContents;
	}

	/**
	 * Used to access the lockedBy field of a LockedDocument 'doc'.
	 * 
	 * @return the lockedBy info from 'doc'
	 * @throws DocumentException if 'doc' is not a LockedDocument or if
	 *         it is not a valid document type
	 */
	public String getLockedBy() throws DocumentException {
		if(!isLocked())
			throw new DocumentException("Document: Document is unlocked, so " +
					"cannot invoke 'getLockedBy()'.");
		else 
			return lockedBy;
	}

	/**
	 * Used to access the lockedUntil field of a LockedDocument 'doc'.
	 * 
	 * @return the lockedUntil info from 'doc'
	 * @throws DocumentException if 'doc' is not a LockedDocument or if
	 *         it is not a valid document type
	 */
	public Date getLockedUntil() throws DocumentException {
		if(!isLocked())
			throw new DocumentException("Document: Document is unlocked, so " +
					"cannot invoke 'getLockedUntil()'.");
		else 
			return lockedUntil;
	}
	
	/**
	 * Used to unlock a LockedDocument 'doc'.
	 * 
	 * @return an UnlockedDocument version of the 'doc' document
	 * @throws DocumentException if 'doc' is not a LockedDocument or if
	 *         it is not a valid document type
	 */
	public UnlockedDocument unlock() throws DocumentException {
		if(!isLocked())
			throw new DocumentException("Document: Document is unlocked, so " +
					"cannot invoke 'unlock()'.");
		else
		{
			this.lockedBy = null;
			this.lockedUntil = null;
			
		    return new UnlockedDocument(this.key, this.title, this.contents);
		}
	}
	
	/**
	 * Used to lock an UnlockedDocument 'doc'.
	 * 
	 * @param lockedBy, the information identifying the client requesting
	 *        the lock
	 * @param lockedUntil, the lock expiration Date 
	 * @return a LockedDocument version of the 'doc' document
	 * @throws DocumentException if 'doc' is not an UnlockedDocument or if
	 *         it is not a valid document type
	 */
	public LockedDocument lock(String lockedBy, Date lockedUntil)
			throws LockUnavailable {
		
		if(!isLocked())
		{
			this.lockedBy = lockedBy;
			this.lockedUntil = lockedUntil;

			return new 
					LockedDocument(lockedBy, lockedUntil, key, title, contents);
		}
		else
		{
	        throw new LockUnavailable("Document: Document is already locked, " +
		        "so cannot invoke 'lock()'.");
		}
	}
	
	/**
	 * Used to determine whether 'doc' is currently locked.
	 * 
	 * @return true if the 'doc' document is locked, false otherwise
	 * @throws DocumentException if 'doc' is not a valid document type
	 */
	public boolean isLocked() {
		return this.lockedUntil != null && (this.lockedUntil).after(new Date()) 
				&& this.lockedBy != null;
	}
	
	/**
	 * Used to obtain the metadata information of the 'doc' document.
	 * 
	 * @return a DocumentMetadata object containing the key and title
	 *         of the 'doc' document.
	 */
	public DocumentMetadata getDocumentMetadata() {
		return new DocumentMetadata(this.getKey(), this.getTitle());
	}
	
}


