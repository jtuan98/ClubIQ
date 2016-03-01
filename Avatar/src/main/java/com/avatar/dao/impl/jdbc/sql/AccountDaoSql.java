package com.avatar.dao.impl.jdbc.sql;

import com.avatar.dto.enums.AccountStatus;

public final class AccountDaoSql {
	public static String INS_ACCOUNT = "INSERT INTO USERS (ID, "
			+ "USERID, MOBILE_IND, MOBILE_NUMBER, HOME_CLUB_ID, "
			+ "EMAIL, PASSWORD, REALNAME, ADDRESS, IMAGE_ID, STATUS, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

	public static String INS_TOKEN = "INSERT INTO USER_ACTIVATION_TOKEN (ID, "
			+ "USER_ID, TOKEN, MOBILE_PIN_FLAG, VALID_TILL, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, ?, NOW())";

	public static String UPD_TOKEN = "UPDATE USER_ACTIVATION_TOKEN SET TOKEN=?, VALID_TILL=?, CREATE_DATE=NOW() "
			+ " WHERE ID = ?";

	public static String INS_DEVICES = "INSERT INTO USER_DEVICES (ID, "
			+ "USER_ID, DEVICE_ID, TANGERINE_HANDSET_ID, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, NOW())";

	public static String INS_ROLES = "INSERT INTO USER_ROLES (ID, "
			+ "USER_ID, ROLE, CREATE_DATE) VALUES (?, ?, ?, NOW())";

	public static String UPD_ACCOUNT_ACTIVATION = "update USERS set ACTIVATION_DATE=?, STATUS='"
			+ AccountStatus.Activated.name()
			+ "' WHERE ID = (SELECT USER_ID FROM USER_ACTIVATION_TOKEN WHERE TOKEN=? AND USER_ID = USERS.ID) AND USERID=? "
			+ "AND STATUS in ('" + AccountStatus.TokenSent.name() + "', '"
			+ AccountStatus.Activated.name() + "', '"
			+ AccountStatus.New.name() + "')";

	public static String UPD_ACCOUNT_DEACTIVATION = "update USERS set PREVIOUS_STATUS = STATUS, SUSPENDED_DATE=?, STATUS='"
			+ AccountStatus.Cancelled.name()
			+ "' WHERE ID = ? AND STATUS <> '"
			+ AccountStatus.Cancelled.name() + "'";

	public static String UPD_ACCOUNT_UNDEACTIVATION = "update USERS set SUSPENDED_DATE=null,STATUS=PREVIOUS_STATUS "
			+ " WHERE ID = ? AND STATUS = '"
			+ AccountStatus.Cancelled.name() + "'";

	public static String UPD_ACCOUNT_STATUS_NOTIFIED = "update USERS set STATUS='"
			+ AccountStatus.TokenSent.name()
			+ "' WHERE USERID = ? AND STATUS in ('"
			+ AccountStatus.New.name()
			+ "', '" + AccountStatus.TokenSent.name() + "')";

	public static String SEL_LINKPHONE_BY_USERID = "select linked.USERID from USERS main, USERS linked where main.id = ? and main.LINKED_ACCOUNT_ID = linked.id";

	public static String UPD_ACCOUNT_LINK = "update USERS set LINKED_ACCOUNT_ID=? where ID = ?";

	public static String UPD_USER_DEVICEID = "update USER_DEVICES set DEVICE_ID=? "
			+ "WHERE USER_ID = (SELECT ID FROM USERS WHERE USERID=?)";

	public static String UPD_USER_TANGERINE_HANDSET_ID = "update USER_DEVICES set TANGERINE_HANDSET_ID=?, DEVICE_ID = ? "
			+ "WHERE USER_ID = (SELECT ID FROM USERS WHERE USERID=?) ";

	public static final String GET_USER_ID_PK = "select ID from USERS where USERID=?";

	public static final String GET_IMAGE_ID_BYUSERID = "select IMAGE_ID from USERS where USERID=?";
	public static final String GET_IMAGE_ID = "select IMAGE_ID from USERS where ID=?";
	public static final String GET_HOME_CLUB_ID = "select HOME_CLUB_ID from USERS where ID=?";

	public static final String UPD_USER_EMAIL = "UPDATE USERS set EMAIL=? where USERID=?";

	public static final String UPD_USER_FULLNAME = "UPDATE USERS set REALNAME=? WHERE USERID=?";

	public static final String SEL_USER = "select * from USERS where USERID = ? ";

	public static final String SEL_USER_BY_PK = "select * from USERS where ID = ? ";

	public static final String SEL_ROLES_BY_USER_ID = "SELECT ROLE from USER_ROLES where USER_ID = ? ";

	public static final String SEL_DEVICE_TANGERINE_HANDSET_ID_BY_USER_ID = "SELECT DEVICE_ID, TANGERINE_HANDSET_ID FROM USER_DEVICES WHERE USER_ID = ? ";

	// SELECT TOKEN FROM USER_DEVICES UD, USER_ACTIVATION_TOKEN UAT, USERS U
	// WHERE VALID_TILL > NOW() AND UAT.USER_ID = U.ID AND UD.USER_ID = U.ID AND
	// USERID = '1234' AND DEVICE_ID = 'deviceId1234'
	public static final String SEL_USERIDPK_BY_USER_ID_DEVICE_ID_TOKEN = "SELECT U.ID FROM USER_DEVICES UD, USER_ACTIVATION_TOKEN UAT, USERS U WHERE VALID_TILL > NOW() AND UAT.USER_ID = U.ID AND UD.USER_ID = U.ID AND USERID = ? AND DEVICE_ID = ? AND UAT.TOKEN=?";
	public static final String SEL_USERIDPK_BY_TOKEN = "SELECT USER_ID FROM USER_ACTIVATION_TOKEN UAT WHERE VALID_TILL > NOW() AND UAT.TOKEN=?";
	public static final String SEL_TOKEN_BY_USERIDPK = "SELECT * FROM USER_ACTIVATION_TOKEN UAT WHERE UAT.USER_ID=?";

	public static String UPD_USER_IMAGE_ID_LINK = "UPDATE USERS SET IMAGE_ID = ? WHERE USERID = ?";

	public static final String GET_USER_ID_BY_DEVICE_ID = "SELECT USERID FROM USERS U, USER_DEVICES UD WHERE USER_ID = U.ID and DEVICE_ID = ? ";

	public static final String UPDATE_NOTICE_INFO = "UPDATE USERS set NOTICED_DATE=?, NOTICED_FLAG=? Where id = ?";

	public static final String SEL_USERS_BY_CLUBID = "select * from USERS where HOME_CLUB_ID = ? and MOBILE_IND = 'Y' ";

	public static final String GET_PREV_STATUS_BY_IDPK = "select PREVIOUS_STATUS from USERS where id = ?";

	public static String VALIDATE_USERID_PASSWD = " SELECT count(*) from USERS where ID = ? and PASSWORD = ? and STATUS = '"
			+ AccountStatus.Activated.name() + "'";

	public static String GET_STATUS_BY_IDPK = "select STATUS from USERS where id = ?";

	public static String VALIDATE_USERID_NOPASSWD = " SELECT count(*) from USERS where ID = ? and STATUS = '"
			+ AccountStatus.Activated.name() + "'";

	public static String INS_SUBAMENITY_EMPLOYEE = "INSERT INTO AMENITY_EMPLOYEE (ID, CLUB_SUBAMENITY_ID, USER_ID, CREATE_DATE) VALUES (?,?,?,NOW())";

	public static String UPD_SUBAMENITY_EMPLOYEE = "UPDATE AMENITY_EMPLOYEE SET CLUB_SUBAMENITY_ID=?, CREATE_DATE=NOW() WHERE USER_ID=? ";

	public static String SEL_SUBAMENITY_ID_BY_USERID = "select distinct CLUB_SUBAMENITY_ID from AMENITY_EMPLOYEE where USER_ID = ? ";

	public static String SEL_SUBAMENITY_USER_EXISTS = "select count(*) from AMENITY_EMPLOYEE where CLUB_SUBAMENITY_ID = ? and USER_ID = ? ";

	public static String INS_NOTES = "INSERT INTO USER_NOTES (ID, USER_ID, NOTE_TEXT, NOTE_DATE) VALUES (?,?,?,?)";

	public static String SEL_NOTESHISTORY_BY_USER_ID = "SELECT * FROM USER_NOTES WHERE USER_ID=? ORDER BY NOTE_DATE DESC";
}
