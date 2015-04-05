package com.avatar.dto.account;

import java.util.LinkedList;
import java.util.List;

import com.avatar.dto.club.AmenityDto;

// Club staffs
public class EmployeeAccountDto extends AccountDto {
	private List<AmenityDto> amenities; // which amenities does this user work
										// for.

	public List<AmenityDto> add(final AmenityDto amenity) {
		if (amenities == null) {
			amenities = new LinkedList<AmenityDto>();
		}
		amenities.add(amenity);
		return amenities;
	}

	public List<AmenityDto> getAmenities() {
		return amenities;
	}

	public void setAmenities(final List<AmenityDto> amenities) {
		this.amenities = amenities;
	}
}
