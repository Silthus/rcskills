-- apply changes
create table rcs_player_history (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  old_level                     integer not null,
  new_level                     integer not null,
  old_exp                       bigint not null,
  new_exp                       bigint not null,
  old_skill_points              integer not null,
  new_skill_points              integer not null,
  data                          json,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcs_player_history primary key (id)
);

alter table rcs_player_levels modify level integer not null;
alter table rcs_player_levels modify skill_points integer not null;
create index ix_rcs_player_history_player_id on rcs_player_history (player_id);
alter table rcs_player_history add constraint fk_rcs_player_history_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

