package com.avatar.dao;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;

public interface AccountDao {
	void activate(String userId, String activationToken, Date activateDate)
			throws NotFoundException;

	Number addNote(Integer userPkId, String noteText, DateTime parseDateTime)
			throws NotFoundException;

	void addSubAmenityToUser(Integer userIdPk, Integer clubSubAmenityIdPk)
			throws NotFoundException, InvalidParameterException;

	void deactivate(String userId, Date deacticateDate)
			throws NotFoundException;

	void deactivate(String userId, Date deacticateDate, boolean cancelNoteFlag)
			throws NotFoundException;

	AccountDto fetch(Integer userIdPk) throws NotFoundException,
	InvalidParameterException;

	AccountDto fetch(String userId) throws NotFoundException,
	InvalidParameterException;

	AccountDto fetchByToken(final String token, final String userId,
			final String deviceId) throws NotFoundException,
			InvalidParameterException;

	List<Privilege> fetchRoles(final Integer userIdPk) throws NotFoundException;

	List<Privilege> fetchRoles(final String userId) throws NotFoundException;

	List<AccountDto> getMembers(int clubIdPk) throws NotFoundException,
	InvalidParameterException;

	List<AccountDto> getMembers(int clubIdPk, DateTime fromDate, boolean populatePicture) throws NotFoundException,
	InvalidParameterException;

	AccountStatus getPreviousStatus(int userIdPk);

	AccountStatus getStatus(int userIdPk);

	String getUserIdByDeviceId(String deviceId) throws NotFoundException;

	int getUserIdPkByUserId(String userId) throws NotFoundException;

	void linkNumbers(String userId, String linkNumber, Date currentDate)
			throws NotFoundException;

	void markStatusAsNotified(String userId) throws NotFoundException;

	// throws NotFoundException, when homeClubId not found!
	void newAccount(AccountDto account, ActivationToken activationToken)
			throws NotFoundException, InvalidParameterException;

	public void populateAccountInfo(final AccountDto account,
			boolean includePicture) throws InvalidParameterException,
			NotFoundException;

	void undeactivate(String userId) throws InvalidParameterException,
	NotFoundException;

	void updateAccountInfoEmail(String userId, String email)
			throws InvalidParameterException, NotFoundException;

	void updateAccountInfoName(String userId, String fullName)
			throws InvalidParameterException, NotFoundException;

	void updateAccountInfoPicture(String userId, String pictureBase64)
			throws InvalidParameterException, NotFoundException;

	void updateNewToken(ActivationToken token)
			throws InvalidParameterException, NotFoundException;

	void updateNoticeInfo(int userIdPk, Date currentDate, boolean agreed)
			throws InvalidParameterException, NotFoundException;

	void updateUserDeviceId(String userId, String deviceId)
			throws InvalidParameterException, NotFoundException;

	void updateUserTangerineHandSetId(String userId, String deviceId,
			String tangerineHandSetId) throws InvalidParameterException, NotFoundException;

	boolean validateUserIdPasswd(String userId, String password)
			throws InvalidParameterException, NotFoundException, InvalidPasswordException;
}
