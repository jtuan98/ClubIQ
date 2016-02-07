alter table BEACON_USERS add constraint beaconuser_beaconid_ibfk_1 foreign key (BEACON_ID)
REFERENCES BEACONS(ID);

alter table BEACON_USERS add constraint beaconuser_userid_ibfk_1 foreign key (USER_ID)
REFERENCES USERS(ID);