-- Fix bug 123
-- ran on aws 1/18
alter table CLUBS modify BODY_TEXT varchar(750);

--fix bug 122
-- ran on aws 1/18
alter table CLUB_SUB_AMENITIES modify BODY_TEXT varchar(750);
