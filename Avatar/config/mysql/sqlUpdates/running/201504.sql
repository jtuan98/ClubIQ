-- 4/4
-- Applied to ec2
CREATE TABLE AMENITY_EMPLOYEE (
	ID    INT(10) NOT NULL PRIMARY KEY, /*PK*/
	CLUB_AMENITY_ID INT(10)  REFERENCES CLUB_AMENITIES(ID),
	USER_ID INT(10) REFERENCES USERS(ID),
	CREATE_DATE DATETIME 
);
CREATE INDEX SA_AMENITYID_IDX ON AMENITY_EMPLOYEE (CLUB_AMENITY_ID, USER_ID);
CREATE INDEX SA_AMENITYID_IDX2 ON AMENITY_EMPLOYEE (USER_ID, CLUB_AMENITY_ID);

-- 4/5
CREATE TABLE AMENITY_TYPES (
	ID INT(10) NOT NULL PRIMARY KEY, /*PK*/
	NAME VARCHAR(10) NOT NULL UNIQUE,
	DESCRIPTION VARCHAR(100) NOT NULL
);
create index AMENITY_TYPES_NAME_IDX on AMENITY_TYPES(NAME);
--insert into AMENITY_TYPES values (1, 'Bar', 'Bar');

ALTER TABLE CLUB_AMENITIES ADD COLUMN (
   	AMENITY_TYPE_ID INT(10) REFERENCES AMENITY_TYPES(ID)
) ;

--the update club_amenities
ALTER TABLE CLUB_AMENITIES MODIFY AMENITY_TYPE_ID INT(10) NOT NULL;

 ALTER TABLE CLUB_AMENITIES ADD FOREIGN KEY (AMENITY_TYPE_ID) REFERENCES AMENITY_TYPES(ID) ;

  ALTER TABLE CLUB_AMENITIES ADD FOREIGN KEY ( CLUB_ID) REFERENCES CLUBS(ID) ;
 
 ALTER TABLE CLUB_AMENITIES ADD FOREIGN KEY (IMAGE_ID) REFERENCES IMAGES(ID) ;


ALTER TABLE SURVEYS DROP COLUMN CLUB_ID;
ALTER TABLE SURVEYS DROP COLUMN CLUB_AMENITY_ID;
ALTER TABLE SURVEYS ADD COLUMN (
   	AMENITY_TYPE_ID INT(10) REFERENCES AMENITY_TYPES(ID) 
) ;
create index SURVEY_IND_AMENITYTYPE on SURVEYS(AMENITY_TYPE_ID);
--update SURVEYS set AMENITY_TYPE_ID = 1; 
update SURVEYS set AMENITY_TYPE_ID=1 where ID=1;
 update SURVEYS set AMENITY_TYPE_ID=1 where ID=2;
 update SURVEYS set AMENITY_TYPE_ID=1 where ID=3;
 update SURVEYS set AMENITY_TYPE_ID=3 where ID=4;
 update SURVEYS set AMENITY_TYPE_ID=3 where ID=5;
 update SURVEYS set AMENITY_TYPE_ID=3 where ID=6;
 update SURVEYS set AMENITY_TYPE_ID=2 where ID=7;
 update SURVEYS set AMENITY_TYPE_ID=2 where ID=8;
 update SURVEYS set AMENITY_TYPE_ID=2 where ID=9;
 update SURVEYS set AMENITY_TYPE_ID=4 where ID=10;
 update SURVEYS set AMENITY_TYPE_ID=4 where ID=11;
 update SURVEYS set AMENITY_TYPE_ID=4 where ID=12;
 update SURVEYS set AMENITY_TYPE_ID=5 where ID=13;
 update SURVEYS set AMENITY_TYPE_ID=5 where ID=14;
 update SURVEYS set AMENITY_TYPE_ID=5 where ID=15;
 update SURVEYS set AMENITY_TYPE_ID=6 where ID=16;
 update SURVEYS set AMENITY_TYPE_ID=6 where ID=17;
 update SURVEYS set AMENITY_TYPE_ID=6 where ID=18;
 update SURVEYS set AMENITY_TYPE_ID=7 where ID=19;
 update SURVEYS set AMENITY_TYPE_ID=7 where ID=20;
 update SURVEYS set AMENITY_TYPE_ID=7 where ID=21;
 update SURVEYS set AMENITY_TYPE_ID=8 where ID=22;
 update SURVEYS set AMENITY_TYPE_ID=8 where ID=23;
 update SURVEYS set AMENITY_TYPE_ID=8 where ID=24;
 update SURVEYS set AMENITY_TYPE_ID=9 where ID=25;
 update SURVEYS set AMENITY_TYPE_ID=9 where ID=26;
 update SURVEYS set AMENITY_TYPE_ID=9 where ID=27;
 update SURVEYS set AMENITY_TYPE_ID=10 where ID=28;
 update SURVEYS set AMENITY_TYPE_ID=10 where ID=29;
 update SURVEYS set AMENITY_TYPE_ID=10 where ID=30;
 update SURVEYS set AMENITY_TYPE_ID=11 where ID=31;
 update SURVEYS set AMENITY_TYPE_ID=11 where ID=32;
 update SURVEYS set AMENITY_TYPE_ID=11 where ID=33;
 update SURVEYS set AMENITY_TYPE_ID=12 where ID=34;
 update SURVEYS set AMENITY_TYPE_ID=12 where ID=35;
 update SURVEYS set AMENITY_TYPE_ID=12 where ID=36;
insert into AMENITY_TYPES values (12, 'Equestrian', 'Equestrian');
commit; 


ALTER TABLE SURVEYS MODIFY AMENITY_TYPE_ID INT(10) NOT NULL;
ALTER TABLE SURVEYS ADD FOREIGN KEY (AMENITY_TYPE_ID) REFERENCES AMENITY_TYPES (ID);

ALTER TABLE BEACONS ADD FOREIGN KEY (CLUB_ID) REFERENCES CLUBS (ID);
ALTER TABLE BEACONS ADD FOREIGN KEY (AMENITY_ID) REFERENCES CLUB_AMENITIES (ID);
ALTER TABLE BEACONS ADD FOREIGN KEY (INSTALLATION_STAFF_ID) REFERENCES USERS (ID);
ALTER TABLE CLUB_APNS_TOKEN ADD FOREIGN KEY (CLUB_AMENITY_ID) REFERENCES CLUB_AMENITIES (ID);

ALTER TABLE PROMOTIONS ADD FOREIGN KEY (CLUB_ID) REFERENCES CLUBS (ID);
ALTER TABLE PROMOTIONS ADD FOREIGN KEY (CLUB_AMENITY_ID) REFERENCES CLUB_AMENITIES (ID);

ALTER TABLE PROMOTION_HISTORY ADD FOREIGN KEY (PROMOTION_ID) REFERENCES PROMOTIONS (ID);
ALTER TABLE PROMOTION_HISTORY ADD FOREIGN KEY (CLUB_ID) REFERENCES CLUBS (ID);
ALTER TABLE PROMOTION_HISTORY ADD FOREIGN KEY (CLUB_AMENITY_ID) REFERENCES CLUB_AMENITIES (ID);
ALTER TABLE PROMOTION_HISTORY ADD FOREIGN KEY (USER_ID) REFERENCES USERS (ID);

ALTER TABLE SURVEY_ANSWERS ADD FOREIGN KEY (SURVEY_ID) REFERENCES SURVEYS (ID);
ALTER TABLE SURVEY_ANSWERS ADD FOREIGN KEY (CLUB_ID) REFERENCES CLUBS (ID);
ALTER TABLE SURVEY_ANSWERS ADD FOREIGN KEY (CLUB_AMENITY_ID) REFERENCES CLUB_AMENITIES (ID);
ALTER TABLE SURVEY_ANSWERS ADD FOREIGN KEY (BEACON_ID) REFERENCES BEACONS (ID);


ALTER TABLE USERS ADD FOREIGN KEY (HOME_CLUB_ID) REFERENCES CLUBS (ID);
ALTER TABLE USERS ADD FOREIGN KEY (IMAGE_ID) REFERENCES IMAGES (ID);

DROP TABLE USER_IMAGES;

DELETE FROM USER_ACTIVATION_TOKEN WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE ID = USER_ID);
ALTER TABLE USER_ACTIVATION_TOKEN ADD FOREIGN KEY (USER_ID) REFERENCES USERS (ID);


ALTER TABLE USER_CLUBS ADD FOREIGN KEY (USER_ID) REFERENCES USERS (ID);
ALTER TABLE USER_CLUBS ADD FOREIGN KEY (CLUB_ID) REFERENCES CLUBS (ID);

ALTER TABLE USER_DEVICES ADD FOREIGN KEY (USER_ID) REFERENCES USERS (ID);







---- 4/16
ALTER TABLE BEACONS ADD COLUMN (INSTALLATION_DATE DATETIME);
update BEACONS set INSTALLATION_DATE = CREATE_DATE;
commit;

---- 4/19
ALTER TABLE CLUBS ADD COLUMN (TIME_ZONE VARCHAR(100) DEFAULT 'US/Pacific');
