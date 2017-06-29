package com.avatar.dto.club;

import java.io.Serializable;

import com.avatar.dto.ImagePic;

public class SubAmenityDto implements Serializable {
	protected Integer id; // primary key.
	protected String subAmenityId;
	protected String description; //
	protected String hoursOfOperation;
	protected ImagePic image;
	protected AmenityDto amenity; // Dining
	protected String header;
	protected String headerSecondary;
	protected String body;
	protected int ordering;

	public SubAmenityDto() {
	}

	public SubAmenityDto(final Integer id) {
		this.id = id;
	}

	public SubAmenityDto(final String subamenityId) {
		this.subAmenityId = subamenityId;
	}

	public AmenityDto getAmenity() {
		return amenity;
	}

	public String getAmenityId() {
		return amenity != null ? amenity.getAmenityId() : null;
	}

	public String getBody() {
		return body;
	}

	public String getDescription() {
		return description;
	}

	public String getHeader() {
		return header;
	}

	public String getHeaderSecondary() {
		return headerSecondary;
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

	public int getOrdering() {
		return ordering;
	}

	public String getSubAmenityId() {
		return subAmenityId;
	}

	public void makeCopy(final SubAmenityDto theCopy) {
		id = theCopy.id;
		subAmenityId = theCopy.subAmenityId;
		description = theCopy.description;
		hoursOfOperation = theCopy.hoursOfOperation;
		image = theCopy.image;
		header = theCopy.header;
		headerSecondary = theCopy.headerSecondary;
		body = theCopy.body;
		if (theCopy.amenity != null) {
			amenity = new AmenityDto();
			amenity.makeCopy(theCopy.amenity);
		}
	}

	public void setAmenity(final AmenityDto amenity) {
		this.amenity = amenity;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setHeader(final String header) {
		this.header = header;
	}

	public void setHeaderSecondary(final String headerSecondary) {
		this.headerSecondary = headerSecondary;
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

	public void setOrdering(final int ordering) {
		this.ordering = ordering;
	}

	public void setSubAmenityId(final String subamenityId) {
		this.subAmenityId = subamenityId;
	}
}
