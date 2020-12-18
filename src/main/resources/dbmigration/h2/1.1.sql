-- apply changes
alter table rcs_player_skills drop constraint if exists ck_rcs_player_skills_status;
alter table rcs_player_skills alter column status varchar(11);
alter table rcs_player_skills add constraint ck_rcs_player_skills_status check ( status in ('ENABLED','DISABLED','ACTIVE','UNLOCKED','INACTIVE','REMOVED','NOT_PRESENT'));
