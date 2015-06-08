package com.avatar.business;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public interface AccountBusiness {

	boolean activateAccount(String activationToken) throws InvalidParameterException;

	boolean activateMobileAccount(String mobileNumber, String deviceId,
			String activationToken) throws InvalidParameterException;

	void addAmenityToUser(String userId, String amenityId)
			throws NotFoundException, InvalidParameterException;

	// Returns a activationToken and expiration date.
	ActivationToken createAccount(AccountDto accountInfo)
			throws NotFoundException, AccountCreationException, InvalidParameterException;

	boolean deactivateAccount(String userId) throws NotFoundException;

	boolean exists(String userId) throws InvalidParameterException;

	AccountDto get(String userId) throws NotFoundException, InvalidParameterException;

	void updateAccountInfo(String userId, String deviceId, String fullName,
			String email, String pictureBase64) throws NotFoundException, InvalidParameterException;

	// SNS token is different than the activation Token.
	void updateUserTangerineHandSetId(String userId, String deviceId,
			String tangerineHandSetId) throws NotFoundException, InvalidParameterException;
}
