ALTER TABLE CLUBS ADD CLUB_PIN VARCHAR(3) UNIQUE;

--For DEV
update CLUB_AMENITIES set AMENITY_TYPE_ID=1;
insert into AMENITY_TYPES values (1, 'Restaurant', 'Restaurant');
commit;
