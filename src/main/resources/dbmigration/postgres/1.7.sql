-- apply changes
alter table rcs_player_skills add column last_used timestamptz;

alter table rcs_skill_slots drop constraint if exists ck_rcs_skill_slots_status;
alter table rcs_skill_slots add constraint ck_rcs_skill_slots_status check ( status in ('IN_USE','FREE','ELIGIBLE'));
