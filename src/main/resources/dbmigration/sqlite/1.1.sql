-- apply changes
create table rcs_datastore (
  id                            varchar(40) not null,
  data                          clob,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_datastore primary key (id)
);

alter table rcs_player_skills add column data_id varchar(40);

