-- Ran on aws 12/18/2015.
alter table CLUB_SUB_AMENITIES add (ORDERING int(10) default 0);

-- Ran on aws 12/18/2015.
alter table CLUB_AMENITIES add (IMAGE_ID INT(10) REFERENCES IMAGES(ID));
	
-- Ran on aws 12/24/2015.
alter table CLUB_SUB_AMENITIES add unique key club_id_subamenityid (club_id, SUBAMENITYID);
-- Ran on aws 12/24/2015.
alter table CLUB_SUB_AMENITIES drop index AMENITYID;
-- Ran on aws 12/24/2015.
alter table CLUB_SUB_AMENITIES drop index SUBAMENITYID;

-- Ran on aws 12/24/2015.
alter table USERS drop column CLUB_MEMBER_ID;

