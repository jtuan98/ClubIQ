rename table CLUB_AMENITIES to CLUB_SUB_AMENITIES;
rename table AMENITY_TYPES to CLUB_AMENITIES;

alter table CLUB_SUB_AMENITIES change AMENITY_NAME NAME varchar(50);
alter table CLUB_SUB_AMENITIES change AMENITYID SUBAMENITYID varchar(50)  NOT NULL;
alter table CLUB_SUB_AMENITIES change AMENITY_TYPE_ID AMENITY_ID INT(10) NOT NULL;
alter table CLUB_SUB_AMENITIES modify column AMENITY_ID INT(10) ;
alter table CLUB_SUB_AMENITIES modify column SUBAMENITYID varchar(50)  NOT NULL;
alter table CLUB_SUB_AMENITIES drop column NAME;

alter table CLUB_AMENITIES add (HEADER varchar(250));
alter table CLUB_AMENITIES change NAME AMENITYID varchar(25);
alter table CLUB_AMENITIES add (CLUB_ID int(10)  REFERENCES CLUBS(ID) );
--need to update club_id
update CLUB_AMENITIES set club_id = (select club_id from CLUB_SUB_AMENITIES where amenity_id=CLUB_AMENITIES.ID limit 1);

alter table CLUB_AMENITIES add (ORDERING int(10) DEFAULT 0 );
alter table BEACONS change AMENITY_ID SUBAMENITY_ID int(10);
alter table CLUB_APNS_TOKEN change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
alter table AMENITY_BLACKOUT change AMENITY_ID SUBAMENITY_ID int(10);

alter table PROMOTIONS change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
alter table USER_RESERVATIONS change AMENITY_ID SUBAMENITY_ID int(10);

--need to drop constraints to non existent table.
select concat(concat('alter table CLUB_SUB_AMENITIES DROP FOREIGN KEY ', CONSTRAINT_NAME),';') 
from information_schema.key_column_usage 
where TABLE_NAME='CLUB_SUB_AMENITIES' and REFERENCED_TABLE_NAME='AMENITY_TYPES';
alter table CLUB_SUB_AMENITIES DROP FOREIGN KEY club_sub_amenities_ibfk_1;    

alter table CLUB_SUB_AMENITIES add constraint club_sub_amenities_ibfk_1 foreign key (AMENITY_ID)
REFERENCES CLUB_AMENITIES(ID);

select concat(concat('alter table CLUB_APNS_TOKEN DROP FOREIGN KEY ', CONSTRAINT_NAME),';') 
from information_schema.key_column_usage 
where TABLE_NAME='CLUB_APNS_TOKEN' and REFERENCED_TABLE_NAME='club_sub_amenities';
alter table CLUB_APNS_TOKEN add constraint club_apns_token_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_SUB_AMENITIES(ID);

alter table AMENITY_EMPLOYEE change CLUB_AMENITY_ID CLUB_SUBAMENITY_ID int(10);
alter table AMENITY_EMPLOYEE add constraint amenity_employee_subamenity_ibfk_1 foreign key (CLUB_SUBAMENITY_ID)
REFERENCES CLUB_AMENITIES(ID);
