-- apply changes
create table rcs_skills (
  id                            varchar(40) not null,
  alias                         varchar(255),
  name                          varchar(255),
  type                          varchar(255),
  description                   varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcs_skills primary key (id)
);

create table rcs_player_levels (
  id                            varchar(40) not null,
  player_id                     varchar(40) not null,
  level                         bigint not null,
  total_exp                     bigint not null,
  skill_points                  bigint not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint uq_rcs_player_levels_player_id unique (player_id),
  constraint pk_rcs_player_levels primary key (id)
);

create table rcs_player_skills (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  skill_id                      varchar(40),
  unlocked                      datetime(6),
  active                        tinyint(1) default 0 not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcs_player_skills primary key (id)
);

create table rcs_players (
  id                            varchar(40) not null,
  name                          varchar(255),
  level_id                      varchar(40),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
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

