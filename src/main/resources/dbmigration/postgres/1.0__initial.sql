-- apply changes
create table rcs_skills (
  id                            uuid not null,
  alias                         varchar(255),
  name                          varchar(255),
  type                          varchar(255),
  description                   varchar(255),
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_skills primary key (id)
);

create table rcs_player_skills (
  id                            uuid not null,
  player_id                     uuid,
  skill_id                      uuid,
  unlocked                      timestamptz,
  active                        boolean default false not null,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_player_skills primary key (id)
);

create table rcs_players (
  id                            uuid not null,
  name                          varchar(255),
  level                         bigint not null,
  exp                           bigint not null,
  skill_points                  bigint not null,
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcs_players primary key (id)
);

create index ix_rcs_skills_alias on rcs_skills (alias);
create index ix_rcs_skills_name on rcs_skills (name);
create index ix_rcs_player_skills_player_id on rcs_player_skills (player_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

create index ix_rcs_player_skills_skill_id on rcs_player_skills (skill_id);
alter table rcs_player_skills add constraint fk_rcs_player_skills_skill_id foreign key (skill_id) references rcs_skills (id) on delete restrict on update restrict;

