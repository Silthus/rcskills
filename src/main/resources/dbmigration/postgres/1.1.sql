-- apply changes
create table rcs_datastore (
  id                            uuid not null,
  data                          json,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_datastore primary key (id)
);

alter table rcs_player_skills add column data_id uuid;

alter table rcs_player_skills add constraint fk_rcs_player_skills_data_id foreign key (data_id) references rcs_datastore (id) on delete restrict on update restrict;

