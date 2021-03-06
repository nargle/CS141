package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Used to contain the entire document along with the locking primitives
 * necessary to modify it.
 */
@PersistenceCapable
public class LockedDocument implements IsSerializable {

	@Persistent
	private String lockedBy = null;
	@Persistent
	private Date lockedUntil = null;
	@PrimaryKey
	@Persistent
	private String key = null;
	@Persistent
	private String title = null;
	@Persistent
	private String contents = null;
	
	// Required by GWT serialization.
	public LockedDocument() {
		
	}
	
	public LockedDocument(String lockedBy, Date lockedUntil, String key,
			String title, String contents) {
		this.lockedBy = lockedBy;
		this.lockedUntil = lockedUntil;
		this.key = key;
		this.title = title;
		this.contents = contents;
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String documentTitle) {
		this.title = documentTitle;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String documentContents) {
		this.contents = documentContents;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public Date getLockedUntil() {
		return lockedUntil;
	}
	
	public UnlockedDocument unlock() {
		return new UnlockedDocument(key, title, contents);
	}
}

