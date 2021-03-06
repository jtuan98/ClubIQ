CREATE DATABASE avatardb;
USE avatardb;

DROP VIEW USERS_VIEW;
DROP TABLE USER_DEVICES;
DROP TABLE USER_ROLES;
DROP TABLE USER_ACTIVATION_TOKEN;
DROP TABLE USER_CLUBS;
DROP TABLE USERS;
DROP TABLE CLUBS;
DROP TABLE IMAGES;

CREATE TABLE IMAGES (
	ID    INT(10) NOT NULL PRIMARY KEY , /*PK*/
	IMAGE_ID VARCHAR(100) NOT NULL,
	IMAGE_BINARY BLOB NOT NULL,
	CREATE_DATE DATETIME 
);

CREATE TABLE CLUBS (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	CLUBID VARCHAR(50) NOT NULL UNIQUE,
	NAME VARCHAR(50),
	ADDRESS VARCHAR(100),
	ZIPCODE VARCHAR(10),
	CITY VARCHAR(50),
	STATE VARCHAR(20),
	PHONE_NUMBER VARCHAR(20),
	HZRESTRICTION VARCHAR(50),
	IMAGE_ID INT(10) REFERENCES IMAGES(ID),
	CREATE_DATE DATETIME 
);
CREATE INDEX CLUBS_IND_CLUBID ON CLUBS (CLUBID);


CREATE TABLE USERS (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	USERID VARCHAR(50) NOT NULL UNIQUE,
	MOBILE_IND CHAR(1),
	MOBILE_NUMBER VARCHAR(25),
	EMAIL VARCHAR(100),
	PASSWORD VARCHAR(100),
	REALNAME VARCHAR(50),
	ADDRESS VARCHAR(250),
	HOME_CLUB_ID INT(10) REFERENCES CLUBS(ID),
	CLUB_MEMBER_ID VARCHAR(50),
	IMAGE_ID INT(10) REFERENCES IMAGES(ID),
	STATUS varchar(10), /*ACTIVATED, PENDING, INACTIVATED*/
	STAFF_ID INT, /*??????*/
	JOIN_RECIPROCAL CHAR(1), /*??????*/
	SHARE_ACCOUNT_USER_ID INT, /*??????*/
	CREATE_DATE DATETIME 
);
CREATE INDEX USERS_IND_USERID ON USERS (USERID);
CREATE INDEX USERS_IND_STATUS ON USERS (STATUS, USERID);

CREATE TABLE USER_CLUBS (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	USER_ID INT(10) REFERENCES USERS(ID) ON DELETE CASCADE,
	CLUB_ID INT(10) REFERENCES CLUBS(ID) ON DELETE CASCADE,
	CREATE_DATE DATETIME 
);
CREATE INDEX USER_CLUBS_IND_USERID ON USER_CLUBS (USER_ID);
CREATE INDEX USER_CLUBS_IND_CLUBID ON USER_CLUBS (CLUB_ID);

CREATE VIEW USERS_VIEW AS SELECT USERID, PASSWORD FROM USERS WHERE STATUS='Activated';

CREATE TABLE USER_ACTIVATION_TOKEN (
	ID    INT(10) NOT NULL PRIMARY KEY , /*PK*/
	USER_ID INT(10) REFERENCES USERS(ID) ON DELETE CASCADE,
	TOKEN VARCHAR(100) NOT NULL,
	MOBILE_PIN_FLAG VARCHAR(1) DEFAULT 'N', 
	VALID_TILL DATE NOT NULL, 
	CREATE_DATE DATETIME 
);
CREATE INDEX USERS_ACT_TK_IND_TOKEN ON USER_ACTIVATION_TOKEN (TOKEN);

CREATE TABLE USER_ROLES (
	ID    INT(10) NOT NULL PRIMARY KEY , /*PK*/
	USER_ID INT(10) REFERENCES USERS(ID) ON DELETE CASCADE,
	ROLE  VARCHAR(10), /*superuser, admin,*/ 
	CREATE_DATE DATETIME 
);

CREATE INDEX USER_ROLES_IND_USERID ON USER_ROLES (USER_ID);
CREATE VIEW USER_ROLES_VIEW AS SELECT USERID, ROLE FROM USERS, USER_ROLES WHERE STATUS='Activated' and USERS.ID = USER_ID;


CREATE TABLE USER_DEVICES (
	ID    INT(10) NOT NULL PRIMARY KEY , /*PK*/
	USER_ID INT(10) REFERENCES USERS(ID) ON DELETE CASCADE,
	DEVICE_ID VARCHAR(100) NOT NULL,
	TANGERINE_HANDSET_ID VARCHAR(100),
	CREATE_DATE DATETIME 
);


CREATE DATABASE sequence;
use sequence;
CREATE TABLE sequence.sequence_data (
    sequence_name varchar(100) NOT NULL,
    sequence_increment int(11) unsigned NOT NULL DEFAULT 1,
    sequence_min_value int(11) unsigned NOT NULL DEFAULT 1,
    sequence_max_value bigint(20) unsigned NOT NULL DEFAULT 18446744073709551615,
    sequence_cur_value bigint(20) unsigned DEFAULT 1,
    sequence_cycle boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY (sequence_name)
) ENGINE=MyISAM;
INSERT INTO sequence.sequence_data ( sequence_name ) VALUES ('ID_SEQ');
COMMIT;

SET GLOBAL log_bin_trust_function_creators = 1;
drop function nextval;
DELIMITER $$
CREATE FUNCTION nextval (seq_name varchar(100))
RETURNS bigint(20) NOT DETERMINISTIC
BEGIN
    DECLARE cur_val bigint(20); 
    SELECT
        sequence_cur_value INTO cur_val
    FROM
        sequence.sequence_data
    WHERE
        sequence_name = seq_name
    ;
    IF cur_val IS NOT NULL THEN
        UPDATE
            sequence.sequence_data
        SET
            sequence_cur_value = IF (
                (sequence_cur_value + sequence_increment) > sequence_max_value,
                IF (
                    sequence_cycle = TRUE,
                    sequence_min_value,
                    NULL
                ),
                sequence_cur_value + sequence_increment
            )
        WHERE
            sequence_name = seq_name
        ;
        SET cur_val = cur_val +1;
    END IF;
    RETURN cur_val;
END;
$$
DELIMITER ;

