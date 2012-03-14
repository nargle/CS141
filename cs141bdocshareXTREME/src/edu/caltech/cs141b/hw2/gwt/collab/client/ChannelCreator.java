package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.Date;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TabBar;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.Parameters;



public class ChannelCreator extends Timer {

	private int iterations;
	private Collaborator collaborator;
	private String docKey, newTitle, newContents;
	protected Socket socket;
	protected CleanupReminder cleanupReminder;
	protected ExpirationTimer expirationTimer;

	public ChannelCreator(Collaborator collaborator, String docKey)
	{
		iterations = 1;
		this.collaborator = collaborator;
		this.docKey = docKey;
		newTitle = null;
		newContents = null;
		socket = null;
		cleanupReminder = new CleanupReminder(collaborator, docKey);
		expirationTimer = null;
	}

	public void run()
	{
		if(collaborator.channelToken != null)
		{
			cleanupReminder.run();
			cleanupReminder.scheduleRepeating(
					Parameters.CLEANUP_REMINDER_INTERVAL);
			ChannelFactory.createChannel(collaborator.channelToken, 
					new ChannelCreatedCallback() {
				@Override
				public void onChannelCreated(Channel channel) {
					socket = channel.open(new SocketListener() {
						@Override
						public void onOpen() {
							/*collaborator.statusUpdate("Channel " + 
									collaborator.docToken + " opened.");*/
							collaborator.acknowledger.
								acknowledgeChannel(docKey);
						}
						@Override
						public void onMessage(String message) {
						    // messageContents[0] is either:
						    // lockexpired
						    // lockgranted
						    // documentdeleted
						    // messageContents[1] is the document key
						    String[] messageContents = message.split(";");
						    String messageType = messageContents[0];
						    String messagePayload = message.substring(
						    		messageType.length() + 1);
						    
						    System.err.println("Message type: " + 
						    			messageType + ".");
						    System.err.println("Message contents: " + 
						    			messagePayload + ".");
						    
						    if (messageType.equals("titleupdated")) {
								newTitle = messagePayload;
								if(newContents != null)
								{
									collaborator.setDocLockedButtons();
									collaborator.lockedDoc.setTitle(
											newTitle);
									collaborator.lockedDoc.setContents(
											newContents);
									
									TabBar tabs = 
											collaborator.openTabs.getTabBar();
									if(newTitle.length() > 
											Parameters.MAX_TITLE_CHARS)
										tabs.setTabText(collaborator.currentTab,
												newTitle.substring(0, 
												Parameters.MAX_TITLE_CHARS - 3)
												+ "...");
									else
										tabs.setTabText(collaborator.currentTab,
												newTitle);
									collaborator.title.setValue(newTitle);
									collaborator.contents.setHTML(newContents);
									
							        expirationTimer = new 
							        		ExpirationTimer(collaborator, 
							        				docKey);
							        expirationTimer.
							        		schedule(Parameters.TIMEOUT);
							        
							        Date expiryTime = new Date();
							        expiryTime.setTime(expiryTime.getTime() + 
							        		Parameters.TIMEOUT);
							        collaborator.statusUpdate("Lock granted;" +
							        		" will expire at " + expiryTime);
							        
							        cleanupReminder.cancel();
							        socket.close();
								}
							}
							else if (messageType.equals("contentsupdated")) {
								newContents = messagePayload;
								if(newTitle != null)
								{
									collaborator.setDocLockedButtons();
									collaborator.lockedDoc.setTitle(
											newTitle);
									collaborator.lockedDoc.setContents(
											newContents);
									
									TabBar tabs = 
											collaborator.openTabs.getTabBar();
									if(newTitle.length() > 
											Parameters.MAX_TITLE_CHARS)
										tabs.setTabText(collaborator.currentTab,
												newTitle.substring(0, 
												Parameters.MAX_TITLE_CHARS - 3)
												+ "...");
									else
										tabs.setTabText(collaborator.currentTab,
												newTitle);
									collaborator.title.setValue(newTitle);
									collaborator.contents.setHTML(newContents);
									
							        ExpirationTimer currentExpirationTimer = new 
							        		ExpirationTimer(collaborator, 
							        				docKey);
							        currentExpirationTimer.
							        		schedule(Parameters.TIMEOUT);
							        
							        Date expiryTime = new Date();
							        expiryTime.setTime(expiryTime.getTime() + 
							        		Parameters.TIMEOUT);
							        collaborator.statusUpdate("Lock granted;" +
							        		" will expire at " + expiryTime);
							        
							        cleanupReminder.cancel();
							        socket.close();
								}
							}
							else if (messageType.equals("documentdeleted")) {
							    int docIndex = collaborator.keyList.indexOf(
							    		messagePayload);
							    if (docIndex == -1) {
							        System.err.println("This collaborator " +
							        		"doesn't have this document open.");
							    }
							    else {
							        collaborator.currentTab = docIndex;
							        collaborator.openTabs.selectTab(
							        		collaborator.currentTab);
						            collaborator.closeTab();
						            collaborator.statusUpdate("Open document " +
						            		"was deleted by another user.");
							    }
							    
							    socket.close();
							}
							else {
							    System.out.println("OOPS, RECEIVED UNHANDLED " +
							    		"MESSAGE!");
							}
						}
						@Override
						public void onError(SocketError error) {
							collaborator.statusUpdate("Channel " + 
									collaborator.channelToken + " errored.");
						}
						@Override
						public void onClose() {
							collaborator.statusUpdate("Channel closed; no " +
									"longer waiting for lock.");
						}
					});
				}
			});
			cancel();
		}
		else if(iterations < Parameters.MAX_CHANNEL_TRIES)
		{
			iterations++;
			schedule(iterations * 1000);
		}
		else
		{
			System.out.println("ChannelCreator expired without receiving" +
					"channel key.");
			collaborator.setDefaultButtons();
			cancel();
		}
	}
}

