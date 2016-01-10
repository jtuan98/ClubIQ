--ran on aws 1/2/2016
alter table USER_NOTES add constraint note_user_idfk1 foreign key (USER_ID) REFERENCES USERS(ID);

--BUG 61
select *
from information_schema.key_column_usage 
where TABLE_NAME='CLUB_AMENITIES';

--ran on aws 1/2/2016
alter table CLUB_AMENITIES drop index NAME;
--ran on aws 1/2/2016
alter table CLUB_AMENITIES add unique key club_id_amenityid (club_id, AMENITYID);

--ran on aws 1/6/2016
alter table  AMENITY_EMPLOYEE add constraint subamenity_userid_fk1 foreign key (USER_ID) REFERENCES USERS(ID);
--ran on aws 1/6/2016
alter table  AMENITY_EMPLOYEE drop foreign key amenity_employee_subamenity_ibfk_1;
--ran on aws 1/6/2016
alter table  AMENITY_EMPLOYEE 
--ran on aws 1/6/2016
add constraint subamenity_employee_subamenity_ibfk_1 
foreign key (CLUB_SUBAMENITY_ID) REFERENCES CLUB_SUB_AMENITIES(ID);

--ran on aws 1/6/2016
alter table USERS add (PREVIOUS_STATUS VARCHAR(10) );

