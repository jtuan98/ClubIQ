USE avatardb;

DROP TABLE PROMOTIONS;

CREATE TABLE PROMOTIONS (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	CLUB_ID INT(10) REFERENCES CLUBS(ID),
	CLUB_AMENITY_ID INT(10) REFERENCES CLUB_AMENITIES(ID),
	TITLE VARCHAR(100) NOT NULL,	
	DETAILS VARCHAR(1000),	
	EFFECTIVE_DATE DATETIME, 
	ENDING_DATE DATETIME, 
	CREATE_DATE DATETIME 
);

DROP TABLE PROMOTION_HISTORY;
CREATE TABLE PROMOTION_HISTORY (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	PROMOTION_ID INT(10) REFERENCES PROMOTIONS(ID),
	CLUB_ID INT(10) REFERENCES CLUBS(ID),
	CLUB_AMENITY_ID INT(10) REFERENCES CLUB_AMENITIES(ID),
	USER_ID INT(10) REFERENCES USERS(ID),
	PROMO_READ VARCHAR(1) NOT NULL DEFAULT 'N',	
	PROMO_PURGE VARCHAR(100),	
	CREATE_DATE DATETIME 
);

insert into PROMOTIONS VALUES (1, 1, 1, 'Tuesday Happy Hour Specials', '50% off drink on every Tuesday Happy Hour 5pm - 7pm', NOW(), NOW()+INTERVAL 30 DAY,  NOW());
commit;


