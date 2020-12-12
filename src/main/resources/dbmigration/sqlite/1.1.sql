-- apply changes
create table rcs_player_history (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  old_level                     integer not null,
  new_level                     integer not null,
  old_exp                       integer not null,
  new_exp                       integer not null,
  old_skill_points              integer not null,
  new_skill_points              integer not null,
  data                          clob,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_player_history primary key (id),
  foreign key (player_id) references rcs_players (id) on delete restrict on update restrict
);

alter table rcs_player_levels alter column level integer;
alter table rcs_player_levels alter column skill_points integer;
