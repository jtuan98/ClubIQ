package com.avatar.business;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.club.CheckInfo;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.AccountSuspendedException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public interface AccountBusiness {

	boolean activateAccount(String activationToken, Date activatedDate) throws InvalidParameterException;

	boolean activateMobileAccount(String mobileNumber, String deviceId,
			String activationToken, Date activatedDate) throws InvalidParameterException;

	void addNote(String memberId, String noteText, DateTime parseDateTime)throws NotFoundException;

	void addSubAmenityToUser(String userId, String amenityId)
			throws NotFoundException, InvalidParameterException;

	void cancelMembership(String userId, Date currentDate)  throws NotFoundException;

	// Returns a activationToken and expiration date.
	ActivationToken createAccount(AccountDto accountInfo)
			throws NotFoundException, AccountCreationException, InvalidParameterException;

	boolean deactivateAccount(String userId) throws NotFoundException;

	boolean exists(String userId) throws InvalidParameterException, AccountSuspendedException;

	AccountDto get(String userId) throws NotFoundException, InvalidParameterException;

	CheckInfo getCheckInfo(String availId) throws NotFoundException;

	List<AccountDto> getMembers(String clubId) throws NotFoundException, InvalidParameterException;

	List<AccountDto> getMembers(String clubId, DateTime fromDate, boolean populatePicture) throws NotFoundException, InvalidParameterException;

	void markStatusAsNotified(String userId) throws NotFoundException;

	void setLinkNumber(String userId, String linkNumber, Date currentDate)throws NotFoundException, InvalidParameterException, AccountSuspendedException;

	void setNoticeInfo(String userId, Date currentDate, boolean agreed)throws NotFoundException, InvalidParameterException;

	void suspend(String memberId, DateTime suspendDate)throws NotFoundException, InvalidParameterException;

	void unsuspend(String memberId) throws NotFoundException, InvalidParameterException;

	void updateAccountInfo(String userId, String deviceId, String fullName,
			String email, String pictureBase64) throws NotFoundException, InvalidParameterException;

	//Returns availId
	String updateCheckInfo(String userId, String requestedClubId,
			String subAmenityId, int numOfPerson, Date requestedDateTime, Date requestedToDateTime) throws NotFoundException;

	// SNS token is different than the activation Token.
	void updateUserTangerineHandSetId(String userId, String deviceId,
			String tangerineHandSetId) throws NotFoundException, InvalidParameterException;
}
