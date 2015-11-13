package com.avatar.dto.account;

import java.io.Serializable;
import java.util.Date;

public class AccountNotes implements Serializable {
	private Integer id; // PK
	private String noteText;
	private Date noteDate;

	public Integer getId() {
		return id;
	}

	public Date getNoteDate() {
		return noteDate;
	}

	public String getNoteText() {
		return noteText;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setNoteDate(final Date noteDate) {
		this.noteDate = noteDate;
	}

	public void setNoteText(final String noteText) {
		this.noteText = noteText;
	}

}
