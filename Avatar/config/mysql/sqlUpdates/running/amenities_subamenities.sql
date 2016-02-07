-- Ran on aws 12/13/2015.
rename table CLUB_AMENITIES to CLUB_SUB_AMENITIES;
-- Ran on aws 12/13/2015.
rename table AMENITY_TYPES to CLUB_AMENITIES;

-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES change AMENITY_NAME NAME varchar(50);
-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES change AMENITYID SUBAMENITYID varchar(50)  NOT NULL;
-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES change AMENITY_TYPE_ID AMENITY_ID INT(10) NOT NULL;
-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES modify column AMENITY_ID INT(10) ;
-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES modify column SUBAMENITYID varchar(50)  NOT NULL;
-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES drop column NAME;

-- Ran on aws 12/13/2015.
alter table CLUB_AMENITIES add (HEADER varchar(250));
-- Ran on aws 12/13/2015.
alter table CLUB_AMENITIES change NAME AMENITYID varchar(25);
-- Ran on aws 12/13/2015.
alter table CLUB_AMENITIES add (CLUB_ID int(10)  REFERENCES CLUBS(ID) );
-- Ran on aws 12/13/2015.
--need to update club_id
update CLUB_AMENITIES set club_id = (select club_id from CLUB_SUB_AMENITIES where amenity_id=CLUB_AMENITIES.ID limit 1);

-- Ran on aws 12/13/2015.
alter table CLUB_AMENITIES add (ORDERING int(10) DEFAULT 0 );
-- Ran on aws 12/13/2015.
alter table BEACONS change AMENITY_ID SUBAMENITY_ID int(10);
-- Ran on aws 12/13/2015.
alter table CLUB_APNS_TOKEN change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
-- Ran on aws 12/13/2015.
alter table AMENITY_BLACKOUT change AMENITY_ID SUBAMENITY_ID int(10);

-- Ran on aws 12/13/2015.
alter table PROMOTIONS change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
-- Ran on aws 12/13/2015.
alter table USER_RESERVATIONS change AMENITY_ID SUBAMENITY_ID int(10);

-- Ran on aws 12/13/2015.
--need to drop constraints to non existent table.
select concat(concat('alter table CLUB_SUB_AMENITIES DROP FOREIGN KEY ', CONSTRAINT_NAME),';') 
from information_schema.key_column_usage 
where TABLE_NAME='CLUB_SUB_AMENITIES' and REFERENCED_TABLE_NAME='AMENITY_TYPES';
alter table CLUB_SUB_AMENITIES DROP FOREIGN KEY club_sub_amenities_ibfk_1;    

-- Ran on aws 12/13/2015.
alter table CLUB_SUB_AMENITIES add constraint club_sub_amenities_ibfk_1 foreign key (AMENITY_ID)
REFERENCES CLUB_AMENITIES(ID);

-- Ran on aws 12/13/2015.
select concat(concat('alter table CLUB_APNS_TOKEN DROP FOREIGN KEY ', CONSTRAINT_NAME),';') 
from information_schema.key_column_usage 
where TABLE_NAME='CLUB_APNS_TOKEN' and REFERENCED_TABLE_NAME='club_sub_amenities';
-- Ran on aws 12/13/2015.
alter table CLUB_APNS_TOKEN add constraint club_apns_token_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_SUB_AMENITIES(ID);

-- Ran on aws 12/13/2015.
alter table AMENITY_EMPLOYEE change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
-- Ran on aws 12/13/2015.
alter table AMENITY_EMPLOYEE add constraint amenity_employee_subamenity_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_AMENITIES(ID);



-- Ran on aws 12/13/2015.
alter table PROMOTION_HISTORY change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
-- Ran on aws 12/13/2015.
alter table PROMOTION_HISTORY add constraint promo_hist_subamenity_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_AMENITIES(ID);


--2016-01-27 Applied to all 3 env
alter table BEACONS DROP FOREIGN KEY beacons_ibfk_2;
alter table BEACONS add constraint beacons_ibfk_2 foreign key (SUBAMENITY_ID)
REFERENCES CLUB_SUB_AMENITIES(ID);

--2016-01-27 Applied to all 3 env
alter table PROMOTION_HISTORY DROP FOREIGN KEY promotion_history_ibfk_3;
alter table PROMOTION_HISTORY DROP FOREIGN KEY promo_hist_subamenity_ibfk_1;  
alter table PROMOTION_HISTORY add constraint promo_hist_subamenity_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_SUB_AMENITIES(ID);
