-- apply changes
alter table rcs_skills add column replace_parent tinyint(1) default 0 not null;
alter table rcs_skills add column replace_parent_slot tinyint(1) default 0 not null;

