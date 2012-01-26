package edu.caltech.cs141b.hw2.gwt.collab.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Used to contain the entire document, without any locking primitives.
 */
public class UnlockedDocument implements IsSerializable {

	private String key = null;
	private String title = null;
	private String contents = null;
	
	// Required by GWT serialization.
	public UnlockedDocument() {
		
	}
	
	public UnlockedDocument(String key, String title, String contents) {
		this.key = key;
		this.title = title;
		this.contents = contents;
	}
	
	public boolean equals(UnlockedDocument b) {
		return (this.key.equals(b.getKey()) &&
				this.title.equals(b.getTitle()) &&
				this.contents.equals(b.getContents()));
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public String getContents() {
		return contents;
	}
	
}

