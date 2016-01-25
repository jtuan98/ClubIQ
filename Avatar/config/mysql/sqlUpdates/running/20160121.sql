-- Fix bug 149
-- ran on aws 1/21
alter table CLUBS add CONCIERGE_FIRSTNAME varchar(100);
alter table CLUBS add CONCIERGE_LASTNAME varchar(100);
alter table CLUBS add CONCIERGE_NOTIF_EMAIL varchar(100);
alter table CLUBS add CONCIERGE_ADMIN_EMAIL varchar(100);
