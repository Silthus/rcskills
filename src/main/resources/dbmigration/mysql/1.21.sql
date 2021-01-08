-- apply changes
alter table rcs_skills add column replaced_skill_ids json default '[]';
alter table rcs_skills add column worlds json default '[]';

alter table rcs_player_skills add column replaced tinyint(1) default 0 not null;

