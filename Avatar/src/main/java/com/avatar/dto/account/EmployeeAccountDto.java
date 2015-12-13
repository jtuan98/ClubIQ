package com.avatar.dto.account;

import com.avatar.dto.club.SubAmenityDto;

// Club staffs
public class EmployeeAccountDto extends AccountDto {
	private SubAmenityDto subAmenity; // which sub amenities does this user work
	// for.

	public EmployeeAccountDto() {
		super();
	}

	public EmployeeAccountDto(final Integer employeeId) {
		setId(employeeId);
	}

	public SubAmenityDto getSubAmenity() {
		return subAmenity;
	}

	public void setSubAmenity(final SubAmenityDto subAmenity) {
		this.subAmenity = subAmenity;
	}
}
