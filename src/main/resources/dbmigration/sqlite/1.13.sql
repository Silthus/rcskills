-- apply changes
alter table rcs_skills add column parent_id varchar(40);

create index ix_rcs_skills_parent_id on rcs_skills (parent_id);

