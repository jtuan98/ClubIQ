package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.ImagePic;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.enums.AccountStatus;

public class AccountDtoMapper implements RowMapper<AccountDto> {

	@Override
	public AccountDto mapRow(final ResultSet rs, final int rowNo)
			throws SQLException {
		final boolean mobile = "Y".equals(rs.getString("MOBILE_IND"));
		final boolean training = "Y".equals(rs.getString("TRAINING"));
		final boolean noticedFlag = "Y".equals(rs.getString("NOTICED_FLAG"));
		final AccountDto retVal = mobile ? new MemberAccountDto()
		: new EmployeeAccountDto();
		retVal.setId(rs.getInt("ID"));
		retVal.setNoticedFlag(noticedFlag);
		retVal.setAddress(rs.getString("ADDRESS"));
		retVal.setEmail(rs.getString("EMAIL"));
		retVal.setTraining("Y".equalsIgnoreCase(rs.getString("TRAINING")));
		retVal.setStatus(AccountStatus.valueOf(rs.getString("STATUS")));
		retVal.setName(rs.getString("REALNAME"));
		retVal.setUserId(rs.getString("USERID"));
		retVal.setTraining(training);
		if (rs.getString("IMAGE_ID") != null) {
			retVal.setPicture(new ImagePic(rs.getInt("IMAGE_ID")));
		}
		if (mobile) {
			final MemberAccountDto mobileAcct = (MemberAccountDto) retVal;
			mobileAcct.setMobileNumber(rs.getString("MOBILE_NUMBER"));
		}

		if (rs.getString("ACTIVATION_DATE") != null) {
			retVal.setActDate(rs.getDate("ACTIVATION_DATE"));
		}

		if (rs.getString("SUSPENDED_DATE") != null) {
			retVal.setActDate(rs.getDate("SUSPENDED_DATE"));
		}

		return retVal;
	}

}
