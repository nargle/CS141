package edu.caltech.cs141b.hw2.gwt.collab.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Used to contain the serialized document metadata.
 * 
 * Note that if it is desired to display or sort by other metadata, such as
 * creation or modification dates, they need only be added to this class.
 */
public class DocumentMetadata implements IsSerializable {
	
	private String key = null;
	private String title = null;
	
	// Required by GWT serialization.
	public DocumentMetadata() {
		
	}
	
	public DocumentMetadata(String key, String title) {
		this.key = key;
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}
	
}

