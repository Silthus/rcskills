-- apply changes
alter table rcs_skills add column disabled_skill_ids clob default '[]';

alter table rcs_player_skills add column disabled int default 0 not null;

