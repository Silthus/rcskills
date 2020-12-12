-- apply changes
create table rcs_player_history (
  id                            uuid not null,
  player_id                     uuid,
  old_level                     integer not null,
  new_level                     integer not null,
  old_exp                       bigint not null,
  new_exp                       bigint not null,
  old_skill_points              integer not null,
  new_skill_points              integer not null,
  data                          json,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_player_history primary key (id)
);

alter table rcs_player_levels alter column level type integer using level::integer;
alter table rcs_player_levels alter column skill_points type integer using skill_points::integer;
create index ix_rcs_player_history_player_id on rcs_player_history (player_id);
alter table rcs_player_history add constraint fk_rcs_player_history_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

