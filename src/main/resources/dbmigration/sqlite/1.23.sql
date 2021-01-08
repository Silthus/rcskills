-- apply changes
alter table rcs_player_skills add column disabled int default 0 not null;

