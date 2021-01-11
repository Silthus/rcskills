-- apply changes
alter table rcs_skills add column replace_parent boolean default false not null;
alter table rcs_skills add column replace_parent_slot boolean default false not null;

