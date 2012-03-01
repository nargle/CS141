package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentException;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Parameters;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {
	
	// private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());
	
	/**
	 * Used to get a list of the currently available documents by using a
	 * PersistenceManager object and an iterator.
	 * 
	 * @return a list of the metadata of the currently available documents
	 */
	@Override
	public List<DocumentMetadata> getDocumentList() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        List<DocumentMetadata> documentList = new LinkedList<DocumentMetadata>();
        
        try {
            tx.begin();
            
            Extent<Document> e = pm.getExtent(Document.class, true);
            Iterator<Document> iter = e.iterator();
            while (iter.hasNext()) //Iterate through stored Document objects
            {
                Document document = (Document) iter.next();
                DocumentMetadata documentMetadata = 
                		document.getDocumentMetadata();
                documentList.add(documentMetadata);
            }
            
            tx.commit();
        }
        finally {
            if (tx.isActive()) {
                // Error occurred so rollback the transaction
                tx.rollback();
            }
            pm.close();
        }
        
        return documentList;
	}
	
	/**
	 * Used to lock an existing document for editing.
	 * 
	 * @param documentKey the key of the document to lock
	 * @return a LockedDocument object containing the current document state
	 *         and the locking primitives necessary to save the document
	 * @throws LockUnavailable if a lock cannot be obtained
	 */
	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    Transaction tx = pm.currentTransaction();
	    LockedDocument lockedDocument = null; //LockedDocument to be returned
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        Date currDate = new Date();
	        // Sets timeout to 30 minutes beyond the current date.
	        currDate.setTime(currDate.getTime() + Parameters.TIMEOUT);
	        
	        lockedDocument =
	        		document.lock(getThreadLocalRequest().getRemoteAddr(), 
	        				currDate);
	        
	        tx.commit();
	    } 
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so rollback the transaction
	            tx.rollback();
	        }
	        pm.close();
	    }
	    
	    return lockedDocument;
	}

	/**
	 * Used to retrieve a document in read-only mode.
	 * 
	 * @param documentKey the key of the document to read
	 * @return an UnlockedDocument object which contains the entire document
	 *         but without any locking primitives
	 */
	@Override
	public UnlockedDocument getDocument(String documentKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        unlockedDocument = new UnlockedDocument(document.getKey(), document.getTitle(), document.getContents());
	        
	        tx.commit();
	    }
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so rollback the transaction
	            tx.rollback();
	        }
	        pm.close();
	    }
	    
	    return unlockedDocument;
	}

	/**
	 * Used to save a currently locked document.
	 * 
	 * @param doc the LockedDocument object returned by lockDocument(), with
	 *         the document properties (but not the locking primitives)
	 *         potentially modified
	 * @return the read-only version of the saved document
	 * @throws LockExpired if the locking primitives in the supplied
	 *         LockedDocument object cannot be used to modify the document
	 */
	@Override
	public UnlockedDocument saveDocument(LockedDocument doc)
			throws LockExpired {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
	    
	    try {
	        tx.begin();
	        
	        Date currDate = new Date();
			
			if (documentExists(doc)) {
				if(doc.getLockedUntil().before(currDate))
					throw new LockExpired("Lock expired!");
				else
				{
		            Document document = 
		            		pm.getObjectById(Document.class, doc.getKey());
		            
		            document.setTitle(doc.getTitle());
		            document.setContents(doc.getContents()); //Update contents
		            unlockedDocument = document.unlock();
				}
	        }
	        
	        else { //Document does not exist in storage already
	        	String key = getThreadLocalRequest().getRemoteAddr() + " " + 
	        			currDate;
	        	unlockedDocument = new UnlockedDocument(key, 
        				doc.getTitle(), doc.getContents());
	            Document document = new Document(null, null, key, doc.getTitle(), doc.getContents());
	            pm.makePersistent(document); //Save new document
	        }
	        
	        tx.commit();
	    }
	    catch(DocumentException e) {
	        	System.err.println("saveDocument(): " + e.getMessage());
            }
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so rollback the transaction
	            tx.rollback();
	        }
	        pm.close();
	    }
	    
	    return unlockedDocument;
	}
	
	/**
	 * Used to release a lock that is no longer needed without saving.
	 * 
	 * @param doc the LockedDocument object returned by lockDocument(); any
	 *         modifications made to the document properties in this case are
	 *         ignored
	 * @throws LockExpired if the locking primitives in the supplied
	 *         LockedDocument object cannot be used to release the lock
	 */	
	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        try {
        	Date currDate = new Date();
			if(doc.getLockedUntil().before(currDate))
				throw new LockExpired("Lock expired!");
        	
            tx.begin();
            
            Document document = pm.getObjectById(Document.class, doc.getKey());
            document.unlock();
            
            tx.commit();
        }
        catch(DocumentException e) {
        	System.err.println("releaseLock(): " + e.getMessage());
            }
        finally {
            if (tx.isActive()) {
                // Error occurred so rollback the transaction
                tx.rollback();
            }
            pm.close();
        }
    }
	
	
	/**
	 * Helper function that tests whether a certain document can be retrieved by
	 * the persistence manager.
	 * 
	 * @param doc
	 * @return
	 */
	private boolean documentExists(LockedDocument doc) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    Extent<Document> e = pm.getExtent(Document.class, true);
        Iterator<Document> iter = e.iterator();
        
	    while (iter.hasNext())
	    {
            Document document = (Document) iter.next();
            if (document.getKey().equals(doc.getKey())) {
	            return true;
	        }
	            
	    }
        
        return false; //Document corresponding to doc not found
	}

}

