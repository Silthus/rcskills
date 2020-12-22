-- apply changes
alter table rcs_skills add column restricted tinyint(1) default 0 not null;

