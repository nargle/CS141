package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Main class for a single Collaborator widget.
 */
public class Collaborator extends Composite implements ClickHandler, ChangeHandler {
	
	protected CollaboratorServiceAsync collabService;
	
	// Track document information.
	protected UnlockedDocument readOnlyDoc = null;
	protected LockedDocument lockedDoc = null;
	
	// Managing available documents.
	protected ListBox documentList = new ListBox();
	private Button refreshList = new Button("Refresh Document List");
	private Button createNew = new Button("Create New Document");
	
	// For displaying document information and editing document content.
	protected TextBox title = new TextBox();
	protected RichTextArea contents = new RichTextArea();
	protected Button refreshDoc = new Button("Refresh Document");
	protected Button lockButton = new Button("Get Document Lock");
	protected Button saveButton = new Button("Save Document");
	
	// Callback objects.
	protected DocLister lister = new DocLister(this);
	protected DocReader reader = new DocReader(this);
	private DocLocker locker = new DocLocker(this);
	protected DocReleaser releaser = new DocReleaser(this);
	private DocSaver saver = new DocSaver(this);
	protected String waitingKey = null;
	
	// Status tracking.
	private VerticalPanel statusArea = new VerticalPanel();
	
	/**
	 * UI initialization.
	 * 
	 * @param collabService
	 */
	public Collaborator(CollaboratorServiceAsync collabService) {
		this.collabService = collabService;
		HorizontalPanel outerHp = new HorizontalPanel();
		outerHp.setWidth("100%");
		VerticalPanel outerVp = new VerticalPanel();
		outerVp.setSpacing(20);
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		vp.add(new HTML("<h2>Available Documents</h2>"));
		documentList.setWidth("100%");
		vp.add(documentList);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(10);
		hp.add(refreshList);
		hp.add(createNew);
		vp.add(hp);
		DecoratorPanel dp = new DecoratorPanel();
		dp.setWidth("100%");
		dp.add(vp);
		outerVp.add(dp);
		
		vp = new VerticalPanel();
		vp.setSpacing(10);
		vp.add(new HTML("<h2>Selected Document</h2>"));
		title.setWidth("100%");
		vp.add(title);
		contents.setWidth("100%");
		vp.add(contents);
		hp = new HorizontalPanel();
		hp.setSpacing(10);
		hp.add(refreshDoc);
		hp.add(lockButton);
		hp.add(saveButton);
		vp.add(hp);
		dp = new DecoratorPanel();
		dp.setWidth("100%");
		dp.add(vp);
		outerVp.add(dp);
		
		outerHp.add(outerVp);
		outerVp = new VerticalPanel();
		outerVp.setSpacing(20);
		dp = new DecoratorPanel();
		dp.setWidth("100%");
		statusArea.setSpacing(10);
		statusArea.add(new HTML("<h2>Console</h2>"));
		dp.add(statusArea);
		outerVp.add(dp);
		outerHp.add(outerVp);
		
		refreshList.addClickHandler(this);
		createNew.addClickHandler(this);
		refreshDoc.addClickHandler(this);
		lockButton.addClickHandler(this);
		saveButton.addClickHandler(this);
		
		documentList.addChangeHandler(this);
		documentList.setVisibleItemCount(10);
		
		setDefaultButtons();
		initWidget(outerHp);
		
		lister.getDocumentList();
	}
	
	/**
	 * Resets the state of the buttons and edit objects to their default.
	 * 
	 * The state of these objects is modified by requesting or obtaining locks
	 * and trying to or successfully saving.
	 */
	protected void setDefaultButtons() {
		refreshDoc.setEnabled(true);
		lockButton.setEnabled(true);
		saveButton.setEnabled(false);
		title.setEnabled(false);
		contents.setEnabled(false);
	}
	
	/**
	 * Behaves similarly to locking a document, except without a key/lock obj.
	 */
	private void createNewDocument() {
		discardExisting(null);
		lockedDoc = new LockedDocument(null, null, null,
				"Enter the document title.",
				"Enter the document contents.");
		locker.gotDoc(lockedDoc);
		History.newItem("new");
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
		} else if (event.getSource().equals(createNew)) {
			createNewDocument();
		} else if (event.getSource().equals(refreshDoc)) {
			if (readOnlyDoc != null) {
				reader.getDocument(readOnlyDoc.getKey());
			}
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
		}
	}

	/* (non-Javadoc)
	 * Intercepts events from the list box.
	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
	 */
	@Override
	public void onChange(ChangeEvent event) {
		if (event.getSource().equals(documentList)) {
			String key = documentList.getValue(documentList.getSelectedIndex());
			discardExisting(key);
			reader.getDocument(key);
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
