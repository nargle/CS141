package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This sets up the basic infrastructure, takes down the loading sign, adds a
 * collaborator widget, and starts history management.  History management is
 * done here, rather than elsewhere, in case at some point the Collaborator
 * widget is not the only involved widget.
 */
public class CollaboratorEntryPoint implements EntryPoint, ValueChangeHandler<String> {
	
	Collaborator collab;

	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CollaboratorServiceAsync collabService =
			GWT.create(CollaboratorService.class);

	/**
	 * Operate on history tokens.
	 * 
	 * @param event
	 */
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		if (!token.equals(collab.getToken())) {
			GWT.log("Got history token: " + token);
			collab.receiveArgs(token);
		}
		else {
			GWT.log("Skipping history token: " + token);
		}
	}

	/**
	 * This is the entry point method, meaning the first method called when
	 * this module is initialized.
	 */
	public void onModuleLoad() {
		collab = new Collaborator(collabService);
		
		// Make the loading display invisible and the application visible.
		RootPanel.get("application").add(collab);
		RootPanel.get("loading").setVisible(false);
		
		// Check for an initial token; if it dne, get the default token.
		String initToken = History.getToken();
		if (initToken.equals("")) {
			History.newItem(collab.getToken());
		}
		
		// Hook into token changes and fire the initial token.
		History.addValueChangeHandler(this);
		History.fireCurrentHistoryState();
	}

}

