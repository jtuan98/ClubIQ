ALTER TABLE CLUBS ADD CLUB_PIN VARCHAR(3) UNIQUE;

--For DEV
insert into AMENITY_TYPES values (1, 'Restaurant', 'Restaurant');
update CLUB_AMENITIES set AMENITY_TYPE_ID=1;
commit;

ALTER TABLE USERS ADD TRAINING VARCHAR(1) DEFAULT 'N';
