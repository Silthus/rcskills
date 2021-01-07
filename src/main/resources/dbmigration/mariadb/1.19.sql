-- apply changes
alter table rcs_skills add column auto_activate tinyint(1) default 0 not null;

