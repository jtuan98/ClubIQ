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
