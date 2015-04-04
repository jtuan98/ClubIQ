package com.avatar.dao;

import java.util.List;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;

public interface AccountDao {
	void activate(String userId, String activationToken)
			throws NotFoundException;

	void deactivate(String userId) throws NotFoundException;

	AccountDto fetch(Integer userIdPk) throws NotFoundException;

	AccountDto fetch(String userId) throws NotFoundException;

	AccountDto fetchByToken(final String token, final String userId,
			final String deviceId) throws NotFoundException;

	List<Privilege> fetchRoles(final Integer userIdPk) throws NotFoundException;

	List<Privilege> fetchRoles(final String userId) throws NotFoundException;

	String getUserIdByDeviceId(String deviceId) throws NotFoundException;

	int getUserIdPkByUserId(String userId) throws NotFoundException;

	void markStatusAsNotified(String userId) throws NotFoundException;

	// throws NotFoundException, when homeClubId not found!
	void newAccount(AccountDto account, ActivationToken activationToken)
			throws NotFoundException;

	public void populateAccountInfo(final AccountDto account);

	void updateAccountInfoEmail(String userId, String email)
			throws NotFoundException;

	void updateAccountInfoName(String userId, String fullName)
			throws NotFoundException;

	void updateAccountInfoPicture(String userId, String pictureBase64)
			throws NotFoundException;

	void updateNewToken(ActivationToken token) throws NotFoundException;

	void updateUserDeviceId(String userId, String deviceId)
			throws NotFoundException;

	void updateUserTangerineHandSetId(String userId, String deviceId,
			String snsToken) throws NotFoundException;

	boolean validateUserIdPasswd(String userId, String password)
			throws NotFoundException, InvalidPasswordException;

}
