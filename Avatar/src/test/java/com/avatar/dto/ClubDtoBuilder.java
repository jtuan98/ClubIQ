package com.avatar.dto;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.DbTimeZone;

public class ClubDtoBuilder implements DtoBuilder<ClubDto> {
	private final ClubDto club = mock(ClubDto.class);

	@Override
	public ClubDto getBuiltInstance() {
		return club;
	}

	public ClubDtoBuilder withAddress(final String address) {
		given(club.getAddress()).willReturn(address);
		return this;
	}

	public ClubDtoBuilder withAmenities(final List<AmenityDto> amenities) {
		given(club.getAmenities()).willReturn(amenities);
		return this;
	}

	public ClubDtoBuilder withCity(final String city) {
		given(club.getCity()).willReturn(city);
		return this;
	}

	public ClubDtoBuilder withClubName(final String clubName) {
		given(club.getClubName()).willReturn(clubName);
		return this;
	}

	public ClubDtoBuilder withClubType(final String clubType) {
		given(club.getClubType()).willReturn(clubType);
		return this;
	}

	public ClubDtoBuilder withHzRestriction(final String hzRestriction) {
		given(club.getHzRestriction()).willReturn(hzRestriction);
		return this;
	}

	public ClubDtoBuilder withId(final ImagePic image) {
		given(club.getImage()).willReturn(image);
		return this;
	}

	public ClubDtoBuilder withId(final Integer id) {
		given(club.getId()).willReturn(id);
		return this;
	}

	public ClubDtoBuilder withPhoneNumber(final String phoneNumber) {
		given(club.getPhoneNumber()).willReturn(phoneNumber);
		return this;
	}

	public ClubDtoBuilder withState(final String state) {
		given(club.getState()).willReturn(state);
		return this;
	}

	public ClubDtoBuilder withTimeZone(final DbTimeZone timeZone) {
		given(club.getTimeZone()).willReturn(timeZone);
		return this;
	}

	public ClubDtoBuilder withWebSite(final String webSite) {
		given(club.getWebSite()).willReturn(webSite);
		return this;
	}

	public ClubDtoBuilder withZipCode(final String zipCode) {
		given(club.getZipCode()).willReturn(zipCode);
		return this;
	}

}
