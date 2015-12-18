alter table CLUB_SUB_AMENITIES add (ORDERING int(10) default 0);

alter table CLUB_AMENITIES add (IMAGE_ID INT(10) REFERENCES IMAGES(ID));
	
