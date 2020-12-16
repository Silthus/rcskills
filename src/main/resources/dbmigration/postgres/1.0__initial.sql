-- apply changes
create table rcs_skills (
  id                            uuid not null,
  alias                         varchar(255),
  name                          varchar(255),
  type                          varchar(255),
  description                   varchar(255),
  config                        json,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_skills primary key (id)
);

create table rcs_levels (
  id                            uuid not null,
  level                         integer not null,
  total_exp                     bigint not null,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_levels primary key (id)
);

create table rcs_level_history (
  id                            uuid not null,
  level_id                      uuid,
  old_level                     integer not null,
  new_level                     integer not null,
  old_exp                       bigint not null,
  new_exp                       bigint not null,
  data                          json,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_level_history primary key (id)
);

create table rcs_player_skills (
  id                            uuid not null,
  player_id                     uuid,
  configured_skill_id           uuid,
  status                        varchar(8),
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint ck_rcs_player_skills_status check ( status in ('ENABLED','DISABLED','ACTIVE','UNLOCKED','INACTIVE','REMOVED')),
  constraint pk_rcs_player_skills primary key (id)
);

create table rcs_players (
  id                            uuid not null,
  name                          varchar(255),
  skill_points                  integer not null,
  level_id                      uuid not null,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint uq_rcs_players_level_id unique (level_id),
  constraint pk_rcs_players primary key (id)
);

create index ix_rcs_skills_alias on rcs_skills (alias);
create index ix_rcs_skills_name on rcs_skills (name);
create index ix_rcs_player_skills_player_id_configured_skill_id on rcs_player_skills (player_id,configured_skill_id);
create index ix_rcs_level_history_level_id on rcs_level_history (level_id);
alter table rcs_level_history add constraint fk_rcs_level_history_level_id foreign key (level_id) references rcs_levels (id) on delete restrict on update restrict;

create index ix_rcs_player_skills_player_id on rcs_player_skills (player_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

create index ix_rcs_player_skills_configured_skill_id on rcs_player_skills (configured_skill_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_configured_skill_id foreign key (configured_skill_id) references rcs_skills (id) on delete restrict on update restrict;

alter table rcs_players add constraint fk_rcs_players_level_id foreign key (level_id) references rcs_levels (id) on delete restrict on update restrict;

