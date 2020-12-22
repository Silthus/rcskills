-- apply changes
alter table rcs_players add column reset_count integer default 0 not null;

