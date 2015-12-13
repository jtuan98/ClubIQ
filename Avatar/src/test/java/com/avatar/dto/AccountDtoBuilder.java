package com.avatar.dto;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Set;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;

public class AccountDtoBuilder implements DtoBuilder<AccountDto> {
	private final AccountDto account;

	public AccountDtoBuilder(final boolean employee) {
		account = employee ? mock(EmployeeAccountDto.class)
				: mock(MemberAccountDto.class);
	}

	@Override
	public AccountDto getBuiltInstance() {
		return account;
	}

	public AccountDtoBuilder withAddress(final String address) {
		given(account.getAddress()).willReturn(address);
		return this;
	}
	public AccountDtoBuilder withSubAmenity(final SubAmenityDto subAmenity) {
		if (subAmenity != null) {
			given(((EmployeeAccountDto)account).getSubAmenity()).willReturn(subAmenity);
		}
		return this;
	}

	public AccountDtoBuilder withDeviceId(final String deviceId) {
		given(account.getDeviceId()).willReturn(deviceId);
		return this;
	}

	public AccountDtoBuilder withEmail(final String email) {
		given(account.getEmail()).willReturn(email);
		return this;
	}

	public AccountDtoBuilder withHomeClub(final ClubDto homeClub) {
		given(account.getHomeClub()).willReturn(homeClub);
		return this;
	}

	public AccountDtoBuilder withId(final Integer id) {
		given(account.getId()).willReturn(id);
		return this;
	}

	public AccountDtoBuilder withMobileNumber(final String mobileNumber) {
		given(account.getMobileNumber()).willReturn(mobileNumber);
		return this;
	}

	public AccountDtoBuilder withName(final String name) {
		given(account.getName()).willReturn(name);
		return this;
	}

	public AccountDtoBuilder withPassword(final String password) {
		given(account.getPassword()).willReturn(password);
		return this;
	}

	public AccountDtoBuilder withPicture(final ImagePic picture) {
		given(account.getPicture()).willReturn(picture);
		return this;
	}

	public AccountDtoBuilder withPrivilege(final Set<Privilege> privileges) {
		given(account.getPriviledges()).willReturn(privileges);
		return this;
	}

	public AccountDtoBuilder withStatus(final AccountStatus status) {
		given(account.getStatus()).willReturn(status);
		return this;
	}

	public AccountDtoBuilder withTangerineHandsetId(
			final String tangerineHandsetId) {
		given(account.getTangerineHandsetId()).willReturn(tangerineHandsetId);
		return this;
	}

	public AccountDtoBuilder withToken(final ActivationToken token) {
		given(account.getToken()).willReturn(token);
		return this;
	}

	public AccountDtoBuilder withTraining(final boolean training) {
		given(account.isTraining()).willReturn(training);
		return this;
	}

	public AccountDtoBuilder withUserId(final String userId) {
		given(account.getUserId()).willReturn(userId);
		return this;
	}
}
