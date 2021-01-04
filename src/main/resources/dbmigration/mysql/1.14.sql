-- apply changes
alter table rcs_player_skills add column parent_id varchar(40);

create index ix_rcs_player_skills_parent_id on rcs_player_skills (parent_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_parent_id foreign key (parent_id) references rcs_player_skills (id) on delete restrict on update restrict;

