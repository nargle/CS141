package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Main class for a single Collaborator widget.
 */
public class Collaborator extends Composite implements ClickHandler {
	
	protected CollaboratorServiceAsync collabService;
	
	// Track document information.
	protected UnlockedDocument readOnlyDoc = null;
	protected LockedDocument lockedDoc = null;
	
	protected ArrayList<LockedDocument> lockedDocList;

	
	// Managing available documents.
	protected ListBox documentList = new ListBox();
	protected Button refreshList = new Button("Refresh");
	private Button loadDoc = new Button("Load Document");
	private Button createNew = new Button("Create Document");
	
	// Used in the tab selection handler
	private boolean creatingDoc;
	protected boolean isReload;
	
	// For displaying document information and editing document content.
	protected int currentTab;
	
	protected ArrayList<TextBox> titleList;
	protected ArrayList<RichTextArea> contentsList;
	protected ArrayList<Button> refreshDocList;
	protected ArrayList<Button> lockButtonList;
	protected ArrayList<Button> saveButtonList;
	protected ArrayList<Button> cancelButtonList;
	protected ArrayList<Button> deleteButtonList;
	protected ArrayList<Button> closeButtonList;
	
	protected TextBox title;
	protected RichTextArea contents;
	protected Button refreshDoc;
	protected Button lockButton;
	protected Button saveButton;
	protected Button cancelButton;
	protected Button deleteButton;
	protected Button closeButton;
	/* REPLACE ABOVE LINES WITH TABPANEL AND ASSOCIATED ELEMENTS */
	protected TabPanel openTabs; // Displays currently opened documents
	protected ArrayList<VerticalPanel> tabWidgs = new ArrayList<VerticalPanel>(); // Panel list for open docs
	
	//Displaying other information
	//private HTML recentInfo; // Recent saves, changes
	//private HTML globalInfo; // Global info about program
	//private TextArea pageTitle; // Title of Page ??
	
	// Callback objects.
	protected DocLister lister = new DocLister(this);
	protected DocReader reader = new DocReader(this);
	private DocLocker locker = new DocLocker(this);
	protected DocReleaser releaser = new DocReleaser(this);
	private DocSaver saver = new DocSaver(this);
	protected DocDeleter deleter = new DocDeleter(this);
	protected String waitingKey = null;
	
	// Status tracking.
	private ScrollPanel statusAreaScroll;
	private VerticalPanel statusArea = new VerticalPanel();
	/* REPLACE ABOVE LINE WITH TEXTAREA FOR RECENT??*/
	
	/**
	 * UI initialization.
	 * 
	 * @param collabService
	 */
    public Collaborator(CollaboratorServiceAsync collabService) {
	    
		this.collabService = collabService;
		
		// Initialize creatingDoc
		creatingDoc = false;
		isReload = false;
		
		// Initialize fields:
		lockedDocList = new ArrayList<LockedDocument>();
		
		titleList = new ArrayList<TextBox>();
		contentsList = new ArrayList<RichTextArea>();
		refreshDocList = new ArrayList<Button>();
		lockButtonList = new ArrayList<Button>();
		saveButtonList = new ArrayList<Button>();
		cancelButtonList = new ArrayList<Button>();
		deleteButtonList = new ArrayList<Button>();
		closeButtonList = new ArrayList<Button>();
		
		
		////HorizontalPanel outerHp = new HorizontalPanel();
		DockPanel dockPan = new DockPanel();
		VerticalPanel westPan = new VerticalPanel();
		VerticalPanel eastPan = new VerticalPanel();
		openTabs = new TabPanel();
		
		//openTabs.getTabBar().addTab("foo");
		
		openTabs.getTabBar().addSelectionHandler(new SelectionHandler<Integer>() {
		        @Override
		        public void onSelection(SelectionEvent<Integer> event) {
		            /* currentTab = openTabs.getTabBar().getSelectedTab();
                    lockButton = lockButtonList.get(currentTab);
                    saveButton = saveButtonList.get(currentTab);
                    cancelButton = cancelButtonList.get(currentTab);
                    deleteButton = deleteButtonList.get(currentTab);
                    closeButton = closeButtonList.get(currentTab); */
		            // Let the user know what they just did.
		            if (!creatingDoc) {
		                int i = event.getSelectedItem();
		                //statusUpdate("You clicked tab " + i);
		                statusUpdate("Selecting tab, index " + i);
		                currentTab = i;
		                refreshDoc = refreshDocList.get(currentTab);
		                lockButton = lockButtonList.get(currentTab);
		                saveButton = saveButtonList.get(currentTab);
		                cancelButton = cancelButtonList.get(currentTab);
		                deleteButton = deleteButtonList.get(currentTab);
		                closeButton = closeButtonList.get(currentTab);
                    
		                refreshDoc.addClickHandler(Collaborator.this);
		                lockButton.addClickHandler(Collaborator.this);
		                saveButton.addClickHandler(Collaborator.this);
		                cancelButton.addClickHandler(Collaborator.this);
		                deleteButton.addClickHandler(Collaborator.this);
		                closeButton.addClickHandler(Collaborator.this);
                    
		                //refreshDoc.setText("REFRESH!");
		                //statusUpdate(refreshDocList.get(1).getText());
		                //statusUpdate("Done clicking tab" + i);
		            }
		            
		        }
		    });
		
		//openTabs.getTabBar().addTab("bar");
		
		/*
		openTabs.addSelectionHandler(new SelectionHandler<Integer>() {
		    @Override
		    public void onSelection(SelectionEvent<Integer> event) {
		        for (int i = 0; i < openTabs.getWidgetCount(); i++) {
		            if (event.getSelectedItem() == i) {
		                currentTab = openTabs.getTabBar().getSelectedTab();
		                lockButton = lockButtonList.get(currentTab);
		                saveButton = saveButtonList.get(currentTab);
		                cancelButton = cancelButtonList.get(currentTab);
		                deleteButton = deleteButtonList.get(currentTab);
		                closeButton = closeButtonList.get(currentTab);
		                statusUpdate("Getting tab: " + ((Integer)currentTab).toString());
		            }
		        }
		    }
		});
		*/
		
		
		//Set up create doc, refresh list, and document list widgets
		westPan.setSpacing(10);
		westPan.setWidth("100%");
		HorizontalPanel docButtons = new HorizontalPanel();
		docButtons.setWidth("100%");
		//docButtons.setHeight("15%");
		docButtons.add(createNew);
		//docButtons.add(loadDoc);
		docButtons.add(refreshList);
		westPan.add(docButtons);
		westPan.add(new HTML("<h2>Documents</h2>"));
		documentList.setVisibleItemCount(15);
		documentList.setWidth("100%");
		//documentList.setHeight("70%");
		westPan.add(documentList);
		//westPan.setCellHeight(docButtons, "15%");
		//westPan.setCellHeight(documentList, "70%");
		westPan.setCellWidth(documentList, "100%");
		westPan.add(loadDoc);
				
		//Set up current document viewing widgets
		openTabs.setWidth("100%");
		VerticalPanel openDocsPanel = new VerticalPanel();
		openDocsPanel.add(openTabs);
		openDocsPanel.setBorderWidth(2);
		openDocsPanel.setWidth("100%");
		openDocsPanel.setHeight("100%");
		
		//Set up recent info and global info widgets
		eastPan.setSpacing(10);
		eastPan.setWidth("100%");
		/*eastPan.add(new HTML("<h2>Recent</h2>"));
		recentInfo = new HTML("ABC was saved 3 minutes ago.<br/>XYZ was saved 2 hours ago.");
		HorizontalPanel recentInfoPanel = new HorizontalPanel();
		recentInfoPanel.setBorderWidth(1);
		recentInfoPanel.add(recentInfo);
		//recentInfoPanel.setHeight("70%");
		eastPan.add(recentInfoPanel);
		recentInfo.setWidth("100%");
		globalInfo = new HTML("<b>Total Views</b>: 4123 <br/><b>Total Saves</b>: 128 <br/>15 Documents.");*
		HorizontalPanel globalInfoPanel = new HorizontalPanel();
		globalInfoPanel.setBorderWidth(1);
		globalInfoPanel.add(globalInfo);
		//globalInfoPanel.setHeight("15%");
		//globalInfoPanel.setHeight("35%");
		
		eastPan.add(globalInfoPanel);
		eastPan.setCellHeight(globalInfoPanel, "15%");
		eastPan.setCellHeight(recentInfoPanel, "70%");*/

	    statusArea.setSpacing(3);
	    //statusArea.add(new HTML("<h2>Console</h2>"));
	    eastPan.add(new HTML("<h2>Console</h2>"));
	    statusAreaScroll = new ScrollPanel(statusArea);
		eastPan.add(statusAreaScroll);
		eastPan.setHeight("100%");
		eastPan.setCellHeight(statusAreaScroll, "90%");
	    //eastPan.setCellHeight(statusAreaScroll, "80%");
		statusAreaScroll.setHeight("100%");
	    //statusArea.setAlwaysShowScrollBars(true);
		//eastPan.add(new HTML("<b>Total Views</b>: 4123 <br/><b>Total Saves</b>: 128 <br/>15 Documents."));
		
		//Set up buttons
		createNew.addClickHandler(this);
		loadDoc.addClickHandler(this);
		refreshList.addClickHandler(this);
		createNew.setEnabled(true);
		loadDoc.setEnabled(true);
		refreshList.setEnabled(true);
		
		////outerHp.setWidth("100%");
		dockPan.setWidth("100%");
		dockPan.setHeight("100%");
		
		//dockPan.add(new Label("North"), DockPanel.NORTH); PUT Logo here if you want?
		dockPan.add(eastPan, DockPanel.EAST);
		dockPan.add(westPan, DockPanel.WEST);
		dockPan.add(openDocsPanel, DockPanel.CENTER);
	
		dockPan.setCellWidth(eastPan, "80%");
		dockPan.setCellWidth(openDocsPanel, "80%");
		dockPan.setCellWidth(westPan, "80%");
		
		dockPan.setCellWidth(eastPan, "25%");
		dockPan.setCellWidth(openDocsPanel, "50%");
		dockPan.setCellWidth(westPan, "25%");
		
		//Fix the heights
		dockPan.setCellHeight(eastPan, westPan.getOffsetHeight() + "px");
		dockPan.setCellHeight(eastPan, eastPan.getOffsetHeight() + "px");
		dockPan.setCellHeight(eastPan, openDocsPanel.getOffsetHeight() + "px");
		
		initWidget(dockPan);
		
		//Wait for 1 second, then load document list
		Timer t = new Timer() {
			public void run() {
				refreshList.click();
			}
		};
		t.schedule(1000);
	}
	
	/**
	 * Opens a document with given title and contents. Returns
	 * the tab panel contents.
	 */
	protected VerticalPanel openDoc(String title, String contents)
	{
	    currentTab = this.openTabs.getWidgetCount();
	    
	    statusUpdate("Opening new document in tab " + currentTab);
		VerticalPanel mainPan = new VerticalPanel();
		mainPan.setSpacing(20);
		mainPan.setHeight("200%");
		
		// Create buttons for viewing document
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setSpacing(20);
		buttons.setHeight("20%");
		
		statusUpdate("Creating buttons...");
		refreshDoc = new Button("Refresh Document");
		lockButton = new Button("Edit");
		saveButton = new Button("Save");
		cancelButton = new Button("Cancel");
		deleteButton = new Button("Delete Document");
		closeButton = new Button("Close Document");
		statusUpdate("Done creating buttons");
		
		refreshDocList.add(currentTab, refreshDoc);
		lockButtonList.add(currentTab, lockButton);
		saveButtonList.add(currentTab, saveButton);
		cancelButtonList.add(currentTab, cancelButton);
		deleteButtonList.add(currentTab, deleteButton);
		closeButtonList.add(currentTab, closeButton);
		
		refreshDoc.addClickHandler(this);
		lockButton.addClickHandler(this);
		saveButton.addClickHandler(this);
		cancelButton.addClickHandler(this);
		deleteButton.addClickHandler(this);
		closeButton.addClickHandler(this);
		
		refreshDoc.setEnabled(true);
		lockButton.setEnabled(true);
		saveButton.setEnabled(false);
		cancelButton.setEnabled(false);
		deleteButton.setEnabled(true);
		closeButton.setEnabled(true);
		
		buttons.add(refreshDoc);
		buttons.add(lockButton);
		buttons.add(saveButton);
		buttons.add(cancelButton);
		buttons.add(closeButton);
		//DOESN'T WORK: buttons.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
		
		mainPan.add(buttons);
		
		// Create text area for title
		this.title = new TextBox();
		//this.title.setEnabled(false);

		this.title.setWidth("100%");
		
		mainPan.add(this.title);
		
		// Create text area for contents
		this.contents = new RichTextArea();
		RichTextArea.Formatter form = this.contents.getFormatter();
		if(form != null) {
			//Do stuff here to show HTML options
		}
		
		this.contents.setHTML(contents);
		//this.contents.setEnabled(false);
		
		this.contents.setHeight("200%");
		this.contents.setWidth("100%");
		
		mainPan.add(this.contents);
		
		setDefaultButtons();
		
		//HTML timeInfo = new HTML("<b>Last saved</b>: 12 minutes ago.");
		//timeInfo.setHeight("20%");
		
		//mainPan.add(timeInfo);
		
		mainPan.add(deleteButton);
		
		openTabs.add(mainPan, title);
		
		openTabs.selectTab(openTabs.getWidgetCount() - 1);
		
		statusUpdate("Done opening new document.");
		
		return mainPan;
	}
	
	/**
	 * Closes the current tab.
	 */
	protected void closeTab()
	{
		if (lockedDoc != null) {
			releaser.releaseLock(lockedDoc);
		}
        refreshDoc = refreshDocList.remove(currentTab);
        lockButton = lockButtonList.remove(currentTab);
        saveButton = saveButtonList.remove(currentTab);
        cancelButton = cancelButtonList.remove(currentTab);
        deleteButton = deleteButtonList.remove(currentTab);
        closeButton = closeButtonList.remove(currentTab);
        openTabs.remove(currentTab);
        statusUpdate("Closing " + currentTab);
        if(currentTab < openTabs.getWidgetCount())
        	openTabs.selectTab(currentTab);
        else if(openTabs.getWidgetCount() > 0)
        	openTabs.selectTab(currentTab - 1);
	}
	
	/**
	 * Resets the state of the buttons and edit objects to their default.
	 * 
	 * The state of these objects is modified by requesting or obtaining locks
	 * and trying to or successfully saving.
	 */
	protected void setDefaultButtons() {
		refreshDoc.setEnabled(true);
		//randomButton.setEnabled(true);
		lockButton.setEnabled(true);
		saveButton.setEnabled(false);
		title.setEnabled(false);
		contents.setEnabled(false);		
		cancelButton.setEnabled(false);
		closeButton.setEnabled(true);
		deleteButton.setEnabled(false);
	}
	
	protected void setDocLockedButtons() {
	    refreshDoc.setEnabled(false);
	    lockButton.setEnabled(false);
	    saveButton.setEnabled(true);
	    title.setEnabled(true);
	    contents.setEnabled(true);
	    cancelButton.setEnabled(true);
	    closeButton.setEnabled(true);
	    deleteButton.setEnabled(true);
	}
	
	/**
	 * Behaves similarly to locking a document, except without a key/lock obj.
	 */
	private void createNewDocument() {
		isReload = false;
	    creatingDoc = true;
		discardExisting(null);
		lockedDoc = new LockedDocument(null, null, null,
				"Untitled",
				"Enter the document contents.");
		//VerticalPanel newDoc = openDoc(lockedDoc.getTitle(), lockedDoc.getContents());
		openDoc(lockedDoc.getTitle(), lockedDoc.getContents());
		creatingDoc = false;
		//openTabs.add(newDoc);
		locker.gotDoc(lockedDoc);
		History.newItem("new");
	}
	
	public void loadDocument() {
		isReload = false;
		creatingDoc = true;
		String key = documentList.getValue(documentList.getSelectedIndex());
		statusUpdate(key);
		reader.getDocument(key);
		///VerticalPanel newTab = 
		//		openDoc(readOnlyDoc.getTitle(), readOnlyDoc.getContents());
		creatingDoc = false;
		///openTabs.add(newTab);
		//History.newItem("loaded");
	}
	
	public void deleteDocument() {
		statusUpdate("Deleting document with key " + this.lockedDoc.getKey());
		deleter.deleteDocument(this.lockedDoc.getKey());
		statusUpdate("Deleted document with key " + this.lockedDoc.getKey());
	}
	
	/**
	 * Returns the currently active token.
	 * 
	 * @return history token which describes the current state
	 */
	protected String getToken() {
		if (lockedDoc != null) {
			if (lockedDoc.getKey() == null) {
				return "new";
			}
			return lockedDoc.getKey();
		} else if (readOnlyDoc != null) {
			return readOnlyDoc.getKey();
		} else {
			return "list";
		}
	}
	
	/**
	 * Modifies the current state to reflect the supplied token.
	 * 
	 * @param args history token received
	 */
	protected void receiveArgs(String args) {
		if (args.equals("list")) {
			readOnlyDoc = null;
			lockedDoc = null;
			title.setValue("");
			contents.setHTML("");
			setDefaultButtons();
		} else if (args.equals("new")) {
			createNewDocument();
		} else {
			reader.getDocument(args);
		}
	}
	
	/**
	 * Adds status lines to the console window to enable transparency of the
	 * underlying processes.
	 * 
	 * @param status the status to add to the console window
	 */
	protected void statusUpdate(String status) {
		while (statusArea.getWidgetCount() > 22) {
		statusArea.remove(1);
		}
		final HTML statusUpd = new HTML(status);
		statusArea.add(statusUpd);
		}
	

	/* (non-Javadoc)
	 * Receives button events.
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource().equals(refreshList)) {
		    History.newItem("list");
            lister.getDocumentList();
		} 
		else if (event.getSource().equals(createNew)) {
			if(openTabs.getWidgetCount() > 7)
				statusUpdate("Couldn't create new document because" 
						+ " there are already 8 documents open.");
			else {
			    statusUpdate("Creating document...");
			    createNewDocument();
			}
		}
		else if (event.getSource().equals(loadDoc)) {
		    //statusUpdate("Loading document...");
			if(openTabs.getWidgetCount() > 7)
				statusUpdate("Couldn't open another document, since there"
						+ " are already 8 documents open.");
			else
				loadDocument();
		}
		else if (event.getSource().equals(refreshDoc)) {
		    statusUpdate("Refreshing document...");
		    isReload = true;
		    if (readOnlyDoc != null) {
		        reader.getDocument(readOnlyDoc.getKey());
		    }
		}
		else if (event.getSource().equals(cancelButton)) {
			releaser.releaseLock(lockedDoc);
		}
        else if (event.getSource().equals(lockButton)) {
            //statusUpdate("Editing document...");
        	//statusUpdate("Requesting editing lock for document");
            if (readOnlyDoc != null) {
            	statusUpdate("Requesting editing lock for document.");
            	lockButton.setEnabled(false);
                locker.lockDocument(readOnlyDoc.getKey());
            }
        }		
		else if (event.getSource().equals(saveButton)) {
		    statusUpdate("Saving document...");
		    isReload = true;
		    lockedDoc.setTitle(title.getValue());
            lockedDoc.setContents(contents.getHTML());
            saver.saveDocument(lockedDoc);
            openTabs.getTabBar().setTabText(currentTab, title.getValue());
		}
		else if (event.getSource().equals(closeButton)) {
			closeTab();
		}
		else if(event.getSource().equals(deleteButton)) {
			deleteDocument();
			closeTab();
			
			// Refreshes the document list.
			History.newItem("list");
            lister.getDocumentList();
		}

		/*
		else if (event.getSource() instanceof Button) {
			//The following code DOESN'T work
			for(int i = 0; i < tabWidgs.size(); i++) {
				if(tabWidgs.get(i).getWidgetIndex((Button)event.getSource()) != -1) {
					tabWidgs.get(i).add(new HTML("You pressed the delete document button!"));
				}
				else if(tabWidgs.get(i).getWidgetIndex(((Button)event.getSource()).getParent()) != -1) {
					tabWidgs.get(i).add(new HTML("You pressed edit, save, cancel, or close button!!!"));
				}
			}
		} 
		*/
		/*if (event.getSource().equals(refreshList)) {
			History.newItem("list");
			lister.getDocumentList();
		} else if (event.getSource().equals(createNew)) {
			createNewDocument();
		} else if (event.getSource().equals(refreshDoc)) {
			if (readOnlyDoc != null) {
				reader.getDocument(readOnlyDoc.getKey());
			}
		} else if (event.getSource().equals(randomButton)) {
			
		} else if (event.getSource().equals(lockButton)) {
			if (readOnlyDoc != null) {
				locker.lockDocument(readOnlyDoc.getKey());
			}
		} else if (event.getSource().equals(saveButton)) {
			if (lockedDoc != null) {
				if (lockedDoc.getTitle().equals(title.getValue()) &&
						lockedDoc.getContents().equals(contents.getHTML())) {
					statusUpdate("No document changes; not saving.");
				}
				else {
					lockedDoc.setTitle(title.getValue());
					lockedDoc.setContents(contents.getHTML());
					saver.saveDocument(lockedDoc);
				}
			}
		}*/
	}
	
	/**
	 * Used to release existing locks when the active document changes.
	 * 
	 * @param key the key of the new active document or null for a new document
	 */
	private void discardExisting(String key) {
		if (lockedDoc != null) {
			if (lockedDoc.getKey() == null) {
				statusUpdate("Discarding new document.");
			}
			else if (!lockedDoc.getKey().equals(key)) {
				releaser.releaseLock(lockedDoc);
			}
			else {
				// Newly active item is the currently locked item.
				return;
			}
			lockedDoc = null;
			setDefaultButtons();
		} else if (readOnlyDoc != null) {
			if (readOnlyDoc.getKey().equals(key)) return;
			readOnlyDoc = null;
		}
	}
}
