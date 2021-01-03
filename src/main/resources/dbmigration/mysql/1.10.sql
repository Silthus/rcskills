-- apply changes
create table rcs_player_bindings (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  skill_id                      varchar(40),
  material                      integer,
  action                        integer,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcs_player_bindings primary key (id)
);

create index ix_rcs_player_bindings_player_id on rcs_player_bindings (player_id);
alter table rcs_player_bindings add constraint fk_rcs_player_bindings_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

create index ix_rcs_player_bindings_skill_id on rcs_player_bindings (skill_id);
alter table rcs_player_bindings add constraint fk_rcs_player_bindings_skill_id foreign key (skill_id) references rcs_player_skills (id) on delete restrict on update restrict;

