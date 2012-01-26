package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Document;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {
	
	private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());

	@Override
	public List<DocumentMetadata> getDocumentList() {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        List<DocumentMetadata> documentList = new LinkedList<DocumentMetadata>();
        
        try {
            tx.begin();
            
            Extent<Document> e = pm.getExtent(Document.class, true);
            Iterator<Document> iter = e.iterator();
            while (iter.hasNext())
            {
                Document document = (Document) iter.next();
                DocumentMetadata documentMetadata = 
                		new DocumentMetadata(document.getKey(), 
                				document.getTitle());
                documentList.add(documentMetadata);
            }
            
            tx.commit();
        }
        catch(Exception e) {
        	System.err.println("saveDocument(): Something awful has " +
        			"happened.");
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

	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {
	    PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
	    PersistenceManager pm = pmf.getPersistenceManager();
	    Transaction tx = pm.currentTransaction();
	    LockedDocument lockedDocument = null;
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        Date currDate = new Date();
	        // Sets time to 30,000 milliseconds beyond the current date
	        currDate.setTime(currDate.getTime() + 30000);
	        
	        lockedDocument =
	        		document.lock(getThreadLocalRequest().getRemoteAddr(), 
	        				currDate);
	        
	        tx.commit();
	    } 
	    catch(Exception e) {
        	System.err.println("lockDocument(): Something awful has " +
        			"happened.");
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

	@Override
	public UnlockedDocument getDocument(String documentKey) {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
	    
	    try {
	        tx.begin();
	        Document document = pm.getObjectById(Document.class, documentKey);
	        
	        unlockedDocument = new UnlockedDocument(document.getKey(), document.getTitle(), document.getContents());
	        
	        tx.commit();
	    }
	    catch(Exception e) {
        	System.err.println("getDocument(): Something awful has " +
        			"happened.");
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

	@Override
	public UnlockedDocument saveDocument(LockedDocument doc)
			throws LockExpired {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        UnlockedDocument unlockedDocument = null;
	    
	    try {
	        tx.begin();
	        
	        Date currDate = new Date();
			if(doc.getLockedUntil().before(currDate))
				throw new LockExpired("Lock Expired!");
			
			else if (documentExists(doc)) {
	            Document document = 
	            		pm.getObjectById(Document.class, doc.getKey());
	            
	            document.setTitle(doc.getTitle());
	            document.setContents(doc.getContents());
	            unlockedDocument = document.unlock();
	            }
	        
	        else {
	        	unlockedDocument = new UnlockedDocument(doc.getKey(), 
        				doc.getTitle(), doc.getContents());
	            Document document = new Document(unlockedDocument);
	            pm.makePersistent(document);
	        }
	        
	        tx.commit();
	    }
	    catch(Exception e) {
        	System.err.println("saveDocument(): Something awful has " +
        			"happened.");
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
	
	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        try {
        	Date currDate = new Date();
			if(doc.getLockedUntil().before(currDate))
				throw new LockExpired("Lock Expired!");
        	
            tx.begin();
            
            Document document = pm.getObjectById(Document.class, doc.getKey());
            document.unlock();
            
            tx.commit();
        }
        catch(Exception e) {
        	System.err.println("releaseLock(): Something awful has " +
        			"happened.");
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
	    PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
	    Extent<Document> e = pm.getExtent(Document.class, true);
        Iterator<Document> iter = e.iterator();
        
        try {
	        while (iter.hasNext())
	        {
	            Document document = (Document) iter.next();
	            if (document.getKey().equals(doc.getKey())) {
	                return true;
	            }
	            
	        }
        }
        catch(Exception x) {
        	System.err.println("documentExists(): Something awful has " +
        			"happened.");
            }
        
        return false;
	}

}

