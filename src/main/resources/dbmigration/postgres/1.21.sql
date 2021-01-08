-- apply changes
alter table rcs_skills add column replaced_skill_ids json default '[]';
alter table rcs_skills add column worlds json default '[]';

alter table rcs_player_skills add column replaced boolean default false not null;

