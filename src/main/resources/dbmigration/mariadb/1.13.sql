-- apply changes
alter table rcs_skills add column parent_id varchar(40);

create index ix_rcs_skills_parent_id on rcs_skills (parent_id);
alter table rcs_skills add constraint fk_rcs_skills_parent_id foreign key (parent_id) references rcs_skills (id) on delete restrict on update restrict;

