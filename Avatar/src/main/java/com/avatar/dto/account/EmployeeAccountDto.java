package com.avatar.dto.account;

import com.avatar.dto.club.AmenityDto;

// Club staffs
public class EmployeeAccountDto extends AccountDto {
	private AmenityDto amenity; // which amenities does this user work
								// for.

	public AmenityDto getAmenity() {
		return amenity;
	}

	public void setAmenity(final AmenityDto amenity) {
		this.amenity = amenity;
	}
}
