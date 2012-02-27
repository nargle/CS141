package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.Timer;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Parameters;



public class ChannelCreator extends Timer {

	private int iterations;
	private Collaborator collaborator;

	public ChannelCreator(Collaborator collaborator)
	{
		iterations = 1;
		this.collaborator = collaborator;
	}

	public void run()
	{
		if(collaborator.docToken != null)
		{
			ChannelFactory.createChannel(collaborator.docToken, 
					new ChannelCreatedCallback() {
				@Override
				public void onChannelCreated(Channel channel) {
					channel.open(new SocketListener() {
						@Override
						public void onOpen() {
							collaborator.statusUpdate("Channel " + 
									collaborator.docToken + " opened.");
							collaborator.acknowledger.
								acknowledgeChannel(
										collaborator.readOnlyDoc.getKey(), 
										collaborator.docToken);
						}
						@Override
						public void onMessage(String message) {
						    // messageContents[0] is either:
						    // lockexpired
						    // lockgranted
						    // documentdeleted
						    // messageContents[1] is the document key
						    String[] messageContents = message.split(";");
						    String status = messageContents[0];
						    String docKey = messageContents[1];
						    
							if (status.equals("lockexpired")) {
							    // Do nothing for now...
							    /*
							    collaborator.setDefaultButtons();
                                collaborator.statusUpdate("Document lock " +
                                        "released.");
                                collaborator.lockedDoc = null;
                                if (collaborator.isCancel) {
                                    collaborator.isReload = true;
                                    collaborator.isCancel = false;
                                }
                                collaborator.isDeleting = false;
                                collaborator.docToken = null;
                                */
							}
							else if (status.equals("lockgranted")) {
								collaborator.setDocLockedButtons();
								collaborator.lockedDoc = 
									new LockedDocument(null, null,
										collaborator.openDocKeys.get(
											collaborator.currentTab),
										collaborator.titleList.get(
											collaborator.currentTab).getText(),
										collaborator.contentsList.get(
											collaborator.currentTab).getText());
								
								
								// New stuff to set up expiration timer
						        
						        System.out.println("Making a new ExpirationTimer");
						        ExpirationTimer currentExpirationTimer = new ExpirationTimer(collaborator, docKey);
						        currentExpirationTimer.schedule(Parameters.TIMEOUT);

						        System.out.println("Done");
							}
							else if (status.equals("documentdeleted")) {
							    int docIndex = collaborator.openDocKeys.indexOf(docKey);
							    if (docIndex == -1) {
							        System.err.println("This collaborator doesn't have this document open.");
							    }
							    else {
							        collaborator.currentTab = docIndex;
							        collaborator.openTabs.selectTab(collaborator.currentTab);
						            collaborator.closeTab();
							    }
							}
							else {
							    System.out.println("OOPS, RECEIVED UNHANDLED MESSAGE!");
							}
						}
						@Override
						public void onError(SocketError error) {
							collaborator.statusUpdate("Channel " + 
									collaborator.docToken + " errored.");
						}
						@Override
						public void onClose() {
							collaborator.statusUpdate("Channel " + 
									collaborator.docToken + " closed.");
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
			cancel();
	}

}

