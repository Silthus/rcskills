-- apply changes
create table rcs_datastore (
  id                            varchar(40) not null,
  data                          json,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcs_datastore primary key (id)
);

alter table rcs_player_skills add column data_id varchar(40);

alter table rcs_player_skills add constraint fk_rcs_player_skills_data_id foreign key (data_id) references rcs_datastore (id) on delete restrict on update restrict;

