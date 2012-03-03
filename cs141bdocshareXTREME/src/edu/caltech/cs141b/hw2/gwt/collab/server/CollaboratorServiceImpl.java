package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// import java.util.logging.Logger;

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

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
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
        List<DocumentMetadata> documentList = new LinkedList<DocumentMetadata>();
        
        try {
            Extent<Document> e = pm.getExtent(Document.class, true);
            Iterator<Document> iter = e.iterator();
            while (iter.hasNext()) //Iterate through stored Document objects
            {
                Document document = (Document) iter.next();
                DocumentMetadata documentMetadata = 
                		document.getDocumentMetadata();
                documentList.add(documentMetadata);
            }
            
            return documentList;
        }
        finally {
            pm.close();
        }
        
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
	public String lockDocument(String documentKey)
			throws LockUnavailable {
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    ChannelService channelService = 
	    		ChannelServiceFactory.getChannelService();
	    String channelKey = channelService.createChannel(
	    		getThreadLocalRequest().getSession().getId());
	    
	    System.err.println(getThreadLocalRequest().getSession().getId());

	    Transaction tx = pm.currentTransaction();
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        document.addChannel(channelKey);
	        System.err.println("document.peekChannel(): " + document.peekChannel());
	        
	        tx.commit();
	    } 
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so rollback the transaction
	            tx.rollback();
	        }
	        pm.close();
	    }
	    
	    return channelKey;
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
        UnlockedDocument unlockedDocument = null;
	    
	    try {
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        unlockedDocument = new UnlockedDocument(document.getKey(), 
	        		document.getTitle(), document.getContents());
	        
	        return unlockedDocument;
	    }
	    finally {
	        pm.close();
	    }
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
	public UnlockedDocument saveDocument(LockedDocument doc, String channelKey)
			throws LockExpired {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
	    
        tx.begin();
        Date currDate = new Date();
        try{
        	if(doc.getKey() != null)
        	{
        		Document document = 
        			pm.getObjectById(Document.class, doc.getKey());
        		System.err.println("Doc Key: " + document.getKey());
        		if(!document.isLocked())
        			throw new LockExpired("saveDocument(): " +
        			"Lock expired, so cannot save.");
        		else if(!document.getLockedBy().equals(
        				getThreadLocalRequest().getSession().getId()))
        			throw new LockExpired("saveDocument(): Another user has " +
        			"acquired the lock, so cannot save.");
        		else
        		{
        			document.setTitle(doc.getTitle());
        			document.setContents(doc.getContents()); //Update contents
        			informLockExpired(document);
        			informLockGranted(document);
        			unlockedDocument = new UnlockedDocument(document.getKey(), 
        					document.getTitle(), document.getContents());
        		}
        	}
        	else
        	{
        		String key = getThreadLocalRequest().getSession().getId() + " " + 
        				currDate;
        		unlockedDocument = new UnlockedDocument(key, 
        				doc.getTitle(), doc.getContents());
        		Document document = 
        			new Document(key, doc.getTitle(), doc.getContents());
        		pm.makePersistent(document); //Save new document
        		
        	}

        	tx.commit();
	    }
	    catch(DocumentException e) {
	        	System.err.println("saveDocument(): " + e.getMessage());
            }
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so roll back the transaction
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
	public UnlockedDocument releaseLock(LockedDocument doc, String channelKey) 
			throws LockExpired {
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
        
        try {
        	tx.begin();
	        Document document = 
	        		pm.getObjectById(Document.class, doc.getKey());
	        if(!document.isLocked())
	        	throw new LockExpired("releaseLock(): " +
	        			"Lock expired, so no need to release.");
	        else if(!document.getLockedBy().equals(
	        		getThreadLocalRequest().getSession().getId()))
	        	throw new LockExpired("releaseLock(): Another user has " +
	        			"acquired the lock, so no need to release.");
	        else
	        {
		        unlockedDocument = document.removeChannel(channelKey);
		        informLockGranted(document);
	        }
	        
	        tx.commit();
        }
        catch(DocumentException e) {
        	System.err.println("releaseLock(): " + e.getMessage());
            }
        finally {
            if (tx.isActive()) {
                // Error occurred so roll back the transaction
                tx.rollback();
            }
            pm.close();
        }
        
        return unlockedDocument;
    }
	
	public void deleteDocument(String documentKey) throws LockExpired
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    Transaction tx = pm.currentTransaction();
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        if(!document.isLocked())
	        	throw new LockExpired("deleteDocument(): " +
	        			"Lock expired, so can not delete document.");
	        else if(!document.getLockedBy().equals(
	        		getThreadLocalRequest().getSession().getId()))
	        	throw new LockExpired("deleteDocument(): Another user has " +
	        			"acquired the lock, so can not delete document.");
	        else
	        {
	        	informDocumentDeleted(document);
	        	pm.deletePersistent(document);
	        }
	        
	        tx.commit();
	    }
	    catch(DocumentException e) {
        	System.err.println("deleteDocument(): " + e.getMessage());
            }
	    finally {
	        if (tx.isActive()) {
	            // Error occurred so roll back the transaction
	            tx.rollback();
	        }
	        pm.close();
	    }
	    
	}
	
	private void informLockGranted(Document doc)
	{
		// If the channelQueue is not empty, grant the lock to the next
		// Collaborator in the queue.
		ChannelService channelService = 
				ChannelServiceFactory.getChannelService();
		String newChannel = doc.peekChannel();
		if(newChannel != null)
		{
			channelService.sendMessage(new 
					ChannelMessage(newChannel, "lockgranted;" + doc.getKey()));
			
			Date expiryDate = new Date();
			expiryDate.setTime(expiryDate.getTime() + Parameters.TIMEOUT);

			doc.setLockedBy(newChannel);
			doc.setLockedUntil(expiryDate);
		}
		else
		{
			doc.setLockedBy(null);
			doc.setLockedUntil(null);
		}
	}
	
	private void informLockExpired(Document doc)
	{
		// Tell the Collaborator that the lock has expired on the current
		// document.
		ChannelService channelService = 
				ChannelServiceFactory.getChannelService();
		String oldChannel = doc.popChannel();

		channelService.sendMessage(new 
				ChannelMessage(oldChannel, "lockexpired;" + doc.getKey()));
	}

	private void informDocumentDeleted(Document doc)
	{
		ChannelService channelService = 
				ChannelServiceFactory.getChannelService();
		
		while(doc.peekChannel() != null)
		{
			String channel = doc.popChannel();
			channelService.sendMessage(new 
					ChannelMessage(channel, "documentdeleted;" + doc.getKey()));
		}
		
	}
	
	public void acknowledgeChannel(String docKey, String channelKey)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Document doc = pm.getObjectById(Document.class, docKey);
		
		System.err.println("doc.peekChannel(): " + doc.peekChannel());
		System.err.println("channelKey: " + channelKey);
		if(doc.peekChannel() != null && doc.peekChannel().equals(channelKey))
			informLockGranted(doc);
	}
}

