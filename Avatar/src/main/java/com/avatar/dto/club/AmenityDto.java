package com.avatar.dto.club;

import java.io.Serializable;

import com.avatar.dto.ImagePic;

public class AmenityDto implements Serializable {
	protected Integer id; // primary key.
	protected String amenityId;
	protected String description;
	protected String hoursOfOperation;
	protected ImagePic image;

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

	public String getHoursOfOperation() {
		return hoursOfOperation;
	}

	public Integer getId() {
		return id;
	}

	public ImagePic getImage() {
		return image;
	}

	public void makeCopy(final AmenityDto theCopy) {
		id = theCopy.id;
		amenityId = theCopy.amenityId;
		description = theCopy.description;
		hoursOfOperation = theCopy.hoursOfOperation;
		image = theCopy.image;
	}

	public void setAmenityId(final String amenityId) {
		this.amenityId = amenityId;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setHoursOfOperation(final String hoursOfOperation) {
		this.hoursOfOperation = hoursOfOperation;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setImage(final ImagePic image) {
		this.image = image;
	}
}
