package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.TimerTask;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;



public class Expiry extends TimerTask {
	
	private String channelKey, docKey;
	
	public Expiry(String channelKey, String docKey)
	{
		this.channelKey = channelKey;
		this.docKey = docKey;
	}
	
	public void run()
	{
		// Tell the Collaborator that the lock has expired on the current
		// document.
		ChannelService channelService = 
	    		ChannelServiceFactory.getChannelService();
		channelService.sendMessage(new 
				ChannelMessage(channelKey, "lockexpired;" + this.docKey));
		
		// Unlock the corresponding document, and grant the lock to the next
		// Collaborator in the queue (if applicable).
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Document doc = pm.getObjectById(Document.class, docKey);
		
		String oldChannel = doc.popChannel();
		
		
		// Ensures this TimerTask will not run again.
		cancel();
	}

}

