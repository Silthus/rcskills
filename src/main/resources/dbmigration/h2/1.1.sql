-- apply changes
create table rcs_datastore (
  id                            uuid not null,
  data                          clob,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_datastore primary key (id)
);

alter table rcs_player_skills add column data_id uuid;

alter table rcs_player_skills add constraint fk_rcs_player_skills_data_id foreign key (data_id) references rcs_datastore (id) on delete restrict on update restrict;

