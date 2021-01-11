-- apply changes
alter table rcs_skills add column replace_parent int default 0 not null;
alter table rcs_skills add column replace_parent_slot int default 0 not null;

