package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.TimerTask;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;



public class ExpirationTimer extends TimerTask {
	
	private String docKey;
	
	public ExpirationTimer(String docKey)
	{
		this.docKey = docKey;
	}
	
	public void run()
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Document doc = pm.getObjectById(Document.class, docKey);
		
		// Unlock the corresponding document, and grant the lock to the next
		// Collaborator in the queue, if this is still the current 
		// ExpirationTimer.
		if(doc.getCurrentExpirationTimer() == this)
		{
			// Tell the Collaborator that the lock has expired on the current
			// document.
			ChannelService channelService = 
		    		ChannelServiceFactory.getChannelService();
			String oldChannel = doc.popChannel();
			
			channelService.sendMessage(new 
					ChannelMessage(oldChannel, "lockexpired;" + this.docKey));
			
			// If the channelQueue is not empty, grant the lock to the next
			// Collaborator in the queue.
			String newChannel = doc.peekChannel();
			if(newChannel != null)
			{
				channelService.sendMessage(new 
					ChannelMessage(newChannel, "lockgranted;" + this.docKey));
				doc.scheduleExpirationTimer(newChannel);
			}
			else
			{
				doc.setLockedBy(null);
				doc.setLockedUntil(null);
			}
		}
		
		// Ensures this TimerTask will not run again.
		cancel();
	}

}

