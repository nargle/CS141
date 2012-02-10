package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Main class for a single Collaborator widget.
 */
public class Collaborator extends Composite implements ClickHandler {

    public static final int MAX_TABS = 8;
    public static final int MAX_TITLE_CHARS = 8;

    protected CollaboratorServiceAsync collabService;

    // Track document information.
    protected UnlockedDocument readOnlyDoc = null;
    protected LockedDocument lockedDoc = null;

    // Managing available documents.
    protected ListBox documentList = new ListBox();
    protected Button refreshList = new Button("Refresh");
    protected Button loadDoc = new Button("Load Document");
    private Button createNew = new Button("Create Document");

    // Used in the tab selection handler
    //private boolean creatingDoc;
    protected boolean isReload;

    // For displaying document information and editing document content.
    protected int currentTab;

    // Array list of widgets that are on each document.
    protected ArrayList<TextBox> titleList;
    protected ArrayList<RichTextArea> contentsList;
    protected ArrayList<String> openDocKeys;
    protected ArrayList<Button> refreshButtonList;
    protected ArrayList<Button> lockButtonList;
    protected ArrayList<Button> saveButtonList;
    protected ArrayList<Button> cancelButtonList;
    protected ArrayList<Button> deleteButtonList;
    protected ArrayList<Button> closeButtonList;

    // Fields that represent the widgets from the currently selected tab.
    protected TextBox title;
    protected RichTextArea contents;
    protected Button refreshButton;
    protected Button lockButton;
    protected Button saveButton;
    protected Button cancelButton;
    protected Button deleteButton;
    protected Button closeButton;
    
    // Controls click handlers for the tabs.
    protected HandlerRegistration refreshHandlerReg, lockHandlerReg, 
        saveHandlerReg, cancelHandlerReg, deleteHandlerReg, closeHandlerReg;
    
    // Contains the currently opened documents.
    protected TabPanel openTabs;

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

    /**
     * UI initialization.
     * 
     * @param collabService
     */
    public Collaborator(CollaboratorServiceAsync collabService) {

        this.collabService = collabService;

        isReload = false;

        // Initialize fields:
        titleList = new ArrayList<TextBox>();
        contentsList = new ArrayList<RichTextArea>();
        openDocKeys = new ArrayList<String>();
        refreshButtonList = new ArrayList<Button>();
        lockButtonList = new ArrayList<Button>();
        saveButtonList = new ArrayList<Button>();
        cancelButtonList = new ArrayList<Button>();
        deleteButtonList = new ArrayList<Button>();
        closeButtonList = new ArrayList<Button>();

        DockPanel dockPan = new DockPanel();
        VerticalPanel westPan = new VerticalPanel();
        VerticalPanel eastPan = new VerticalPanel();
        openTabs = new TabPanel();

        // Updates the current button whenever a new tab is selected.
        openTabs.getTabBar().addSelectionHandler(new SelectionHandler<Integer>() 
            {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                // Remove previous click handlers.
                if(refreshHandlerReg != null)
                    refreshHandlerReg.removeHandler();
                if(lockHandlerReg != null)
                    lockHandlerReg.removeHandler();
                if(saveHandlerReg != null)
                    saveHandlerReg.removeHandler();
                if(cancelHandlerReg != null)
                    cancelHandlerReg.removeHandler();
                if(deleteHandlerReg != null)
                    deleteHandlerReg.removeHandler();
                if(closeHandlerReg != null)
                    closeHandlerReg.removeHandler();

                // Update the current buttons.
                int i = event.getSelectedItem();
                currentTab = i;
                refreshButton = refreshButtonList.get(currentTab);
                lockButton = lockButtonList.get(currentTab);
                saveButton = saveButtonList.get(currentTab);
                cancelButton = cancelButtonList.get(currentTab);
                deleteButton = deleteButtonList.get(currentTab);
                closeButton = closeButtonList.get(currentTab);

                // Update the click handlers.
                refreshHandlerReg = 
                    refreshButton.addClickHandler(Collaborator.this);
                lockHandlerReg = 
                    lockButton.addClickHandler(Collaborator.this);
                saveHandlerReg = 
                    saveButton.addClickHandler(Collaborator.this);
                cancelHandlerReg = 
                    cancelButton.addClickHandler(Collaborator.this);
                deleteHandlerReg = 
                    deleteButton.addClickHandler(Collaborator.this);
                closeHandlerReg = 
                    closeButton.addClickHandler(Collaborator.this);
            }
        });

        //Set up create doc, refresh list, and document list widgets
        westPan.setSpacing(10);
        westPan.setWidth("100%");
        HorizontalPanel docButtons = new HorizontalPanel();
        docButtons.setWidth("100%");
        docButtons.add(createNew);
        docButtons.add(refreshList);
        westPan.add(docButtons);
        westPan.add(new HTML("<h2>Documents</h2>"));
        documentList.setVisibleItemCount(15);
        documentList.setWidth("100%");
        westPan.add(documentList);
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

        statusArea.setSpacing(3);
        eastPan.add(new HTML("<h2>Console</h2>"));
        statusAreaScroll = new ScrollPanel(statusArea);
        eastPan.add(statusAreaScroll);
        eastPan.setHeight("100%");
        eastPan.setCellHeight(statusAreaScroll, "90%");
        statusAreaScroll.setHeight("100%");
        //statusArea.setAlwaysShowScrollBars(true);

        //Set up buttons
        createNew.addClickHandler(this);
        loadDoc.addClickHandler(this);
        refreshList.addClickHandler(this);
        createNew.setEnabled(true);
        loadDoc.setEnabled(true);
        refreshList.setEnabled(true);

        dockPan.setWidth("100%");
        dockPan.setHeight("100%");

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
     * 
     * @param title the title of the document.
     * @param contents the contents of the document.
     * 
     * @return the newly created tab.
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

        refreshButton = new Button("Refresh Document");
        lockButton = new Button("Edit");
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        deleteButton = new Button("Delete Document");
        closeButton = new Button("Close Document");

        refreshButtonList.add(currentTab, refreshButton);
        lockButtonList.add(currentTab, lockButton);
        saveButtonList.add(currentTab, saveButton);
        cancelButtonList.add(currentTab, cancelButton);
        deleteButtonList.add(currentTab, deleteButton);
        closeButtonList.add(currentTab, closeButton);

        refreshButton.setEnabled(true);
        lockButton.setEnabled(true);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(true);
        closeButton.setEnabled(true);

        buttons.add(refreshButton);
        buttons.add(lockButton);
        buttons.add(saveButton);
        buttons.add(cancelButton);
        buttons.add(closeButton);

        mainPan.add(buttons);

        // Create text area for title
        this.title = new TextBox();
        this.title.setWidth("100%");
        this.title.setText(title);

        mainPan.add(this.title);

        // Create text area for contents
        this.contents = new RichTextArea();
        RichTextArea.Formatter form = this.contents.getFormatter();
        if(form != null) {
            //Do stuff here to show HTML options
        }

        this.contents.setHTML(contents);

        this.contents.setHeight("100%");
        this.contents.setWidth("100%");
        
        mainPan.setSpacing(10);

        mainPan.add(this.contents);

        setDefaultButtons();

        mainPan.add(deleteButton);

        if(title.length() > MAX_TITLE_CHARS)
            openTabs.add(mainPan, title.substring(0, MAX_TITLE_CHARS - 3) + "...");
        else
            openTabs.add(mainPan, title);

        openTabs.selectTab(openTabs.getWidgetCount() - 1);

        statusUpdate("Done opening new document.");

        return mainPan;
    }

    /**
     * Resets the state of the buttons and edit objects to their default.
     * 
     * The state of these objects is modified by requesting or obtaining locks
     * and trying to or successfully saving.
     */
    protected void setDefaultButtons() {
        refreshButton.setEnabled(true);
        //randomButton.setEnabled(true);
        lockButton.setEnabled(true);
        saveButton.setEnabled(false);
        title.setEnabled(false);
        contents.setEnabled(false);  
        cancelButton.setEnabled(false);
        closeButton.setEnabled(true);
        deleteButton.setEnabled(false);
    }

    /**
     * Resets the state of the buttons and edit objects to correspond to 
     * a document whose lock you have retrieved.
     */
    protected void setDocLockedButtons() {
        refreshButton.setEnabled(false);
        lockButton.setEnabled(false);
        saveButton.setEnabled(true);
        title.setEnabled(true);
        contents.setEnabled(true);
        cancelButton.setEnabled(true);
        closeButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    
    /**
     * Closes the current tab.
     */
    protected void closeTab()
    {
        // Release lock only if you already have a lock (i.e. the document has
        // been saved at least once).
        if (lockedDoc != null && lockedDoc.getKey() != null) {
            releaser.releaseLock(lockedDoc);
        }
        // Update array lists that contain information from the current 
        // document.
        refreshButton = refreshButtonList.remove(currentTab);
        lockButton = lockButtonList.remove(currentTab);
        saveButton = saveButtonList.remove(currentTab);
        cancelButton = cancelButtonList.remove(currentTab);
        deleteButton = deleteButtonList.remove(currentTab);
        closeButton = closeButtonList.remove(currentTab);
        openTabs.remove(currentTab);
        // Open the previous tab.
        if(currentTab < openTabs.getWidgetCount())
            openTabs.selectTab(currentTab);
        else if(openTabs.getWidgetCount() > 0)
            openTabs.selectTab(currentTab - 1);
    }

    /**
     * Creates a new document and opens it in a new tab.
     */
    private void createNewDocument() {
        // Do not reload the tab when creating a new document.
        isReload = false;
        
        openDocKeys.add(null);
        // Relinquish existing lock.
        discardExisting(null);
        
        // Create the new document as a locked document
        lockedDoc = new LockedDocument(null, null, null, 
                "Untitled", "Enter the document contents.");
        // Open it in a new tab.
        openDoc(lockedDoc.getTitle(), lockedDoc.getContents());
        // Get a new lock.
        locker.gotDoc(lockedDoc);
        History.newItem("new");
    }
    
    /**
     * Saves the current document.
     */
    public void saveDocument(){
        statusUpdate("Saving document...");
        // Reload the tab after saving the document.
        isReload = true;
        lockedDoc.setTitle(title.getValue());
        lockedDoc.setContents(contents.getHTML());
        saver.saveDocument(lockedDoc);
        openDocKeys.set(currentTab, readOnlyDoc.getKey());
    }

    /**
     * Loads the document that is currently selected in the document list.
     */
    public void loadDocument() {
        // Don't reload the tab when loading the document.
        isReload = false;
        loadDoc.setEnabled(false);
        String key = documentList.getValue(documentList.getSelectedIndex());
        openDocKeys.add(key);
        statusUpdate(key);
        reader.getDocument(key);
    }

    /**
     * Delete the current document.
     */
    public void deleteDocument() {
        deleter.deleteDocument(this.lockedDoc.getKey());
        statusUpdate("Document deleted.");
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
        while (statusArea.getWidgetCount() > 15) {
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
        // Refresh the document list.
        if (event.getSource().equals(refreshList)) {
            History.newItem("list");
            lister.getDocumentList();
        } 
        // Create a new document.
        else if (event.getSource().equals(createNew)) {
            if (openTabs.getWidgetCount() > MAX_TABS - 1)
                statusUpdate("Couldn't create new document because" 
                        + " there are already " + MAX_TABS + " documents open.");
            else {
                statusUpdate("Creating document...");
                createNewDocument();
            }
        }
        // Loads the currently selected document.
        else if (event.getSource().equals(loadDoc)) {
            if (openDocKeys.contains(documentList.getValue(
                    documentList.getSelectedIndex()))) {
                statusUpdate("Error: You already have that document open!");
            }
            else if (openTabs.getWidgetCount() > MAX_TABS - 1)
                statusUpdate("Couldn't open another document, since there"
                        + " are already " + MAX_TABS + " documents open.");
            else
                loadDocument();
        }
        // Refreshes the current document.
        else if (event.getSource().equals(refreshButton)) {
            statusUpdate("Refreshing document...");
            isReload = true;
            if (readOnlyDoc != null) {
                reader.getDocument(readOnlyDoc.getKey());
            }
        }
        // Cancels editing of the current document.
        else if (event.getSource().equals(cancelButton)) {
            releaser.releaseLock(lockedDoc);
        }
        // Enables enditing of the current document.
        else if (event.getSource().equals(lockButton)) {
            if (readOnlyDoc != null) {
                statusUpdate("Requesting editing lock for document.");
                lockButton.setEnabled(false);
                locker.lockDocument(readOnlyDoc.getKey());
            }
        }
        // Saves the current document.
        else if (event.getSource().equals(saveButton)) {
            saveDocument();
        }
        // Closes the current document.
        else if (event.getSource().equals(closeButton)) {
            openDocKeys.remove(currentTab);
            closeTab();
        }
        // Deletes the current document.
        else if(event.getSource().equals(deleteButton)) {
            openDocKeys.remove(currentTab);
            deleteDocument();
            closeTab();
            History.newItem("list");
            lister.getDocumentList();
        }
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
