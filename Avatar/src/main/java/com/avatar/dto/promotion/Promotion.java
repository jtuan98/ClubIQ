package com.avatar.dto.promotion;

import java.io.Serializable;
import java.util.Date;

import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;

public class Promotion implements Serializable {

	protected Integer id; // primary key.
	protected ClubDto club;
	protected SubAmenityDto subAmenity;
	protected String title;
	protected String description;
	protected Date effectiveDate;
	protected Date endingDate;

	public Promotion() {
	}

	public ClubDto getClub() {
		return club;
	}

	public String getDescription() {
		return description;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public Date getEndingDate() {
		return endingDate;
	}

	public Integer getId() {
		return id;
	}

	public SubAmenityDto getSubAmenity() {
		return subAmenity;
	}

	public String getTitle() {
		return title;
	}

	public void setClub(final ClubDto club) {
		this.club = club;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setEffectiveDate(final Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public void setEndingDate(final Date endingDate) {
		this.endingDate = endingDate;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setSubAmenity(final SubAmenityDto subAmenity) {
		this.subAmenity = subAmenity;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

}
