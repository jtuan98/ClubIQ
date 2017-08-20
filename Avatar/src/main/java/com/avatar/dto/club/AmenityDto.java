package com.avatar.dto.club;

import java.io.Serializable;

import com.avatar.dto.ImagePic;

public class AmenityDto implements Serializable {
	protected Integer id; // primary key.
	protected String amenityId;
	protected String description; //
	protected String header;
	protected ImagePic image;
	protected int ordering;

	public AmenityDto() {
	}

	public AmenityDto(final Integer id) {
		this.id = id;
	}

	public AmenityDto(final String amenityId) {
		this.amenityId = amenityId;
	}

	public String getAmenityId() {
		return amenityId;
	}

	public String getDescription() {
		return description;
	}

	public String getHeader() {
		return header;
	}

	public Integer getId() {
		return id;
	}

	public ImagePic getImage() {
		return image;
	}

	public int getOrdering() {
		return ordering;
	}

	public void makeCopy(final AmenityDto theCopy) {
		id = theCopy.id;
		amenityId = theCopy.amenityId;
		description = theCopy.description;
		header = theCopy.header;
	}

	public void setAmenityId(final String amenityId) {
		this.amenityId = amenityId;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setHeader(final String header) {
		this.header = header;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setImage(final ImagePic image) {
		this.image = image;
	}

	public void setOrdering(final int ordering) {
		this.ordering = ordering;
	}
}
