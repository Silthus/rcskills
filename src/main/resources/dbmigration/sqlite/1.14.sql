-- apply changes
alter table rcs_player_skills add column parent_id varchar(40);

create index ix_rcs_player_skills_parent_id on rcs_player_skills (parent_id);

