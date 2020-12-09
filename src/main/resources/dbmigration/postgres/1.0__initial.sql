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

create table rcs_player_levels (
  id                            uuid not null,
  player_id                     uuid not null,
  level                         bigint not null,
  total_exp                     bigint not null,
  skill_points                  bigint not null,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint uq_rcs_player_levels_player_id unique (player_id),
  constraint pk_rcs_player_levels primary key (id)
);

create table rcs_player_skills (
  id                            uuid not null,
  player_id                     uuid,
  skill_id                      uuid,
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
  level_id                      uuid,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint uq_rcs_players_level_id unique (level_id),
  constraint pk_rcs_players primary key (id)
);

create index ix_rcs_skills_alias on rcs_skills (alias);
create index ix_rcs_skills_name on rcs_skills (name);
alter table rcs_player_levels add constraint fk_rcs_player_levels_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

create index ix_rcs_player_skills_player_id on rcs_player_skills (player_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

create index ix_rcs_player_skills_skill_id on rcs_player_skills (skill_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_skill_id foreign key (skill_id) references rcs_skills (id) on delete restrict on update restrict;

alter table rcs_players add constraint fk_rcs_players_level_id foreign key (level_id) references rcs_player_levels (id) on delete restrict on update restrict;

