-- apply changes
create table rcs_skills (
  id                            varchar(255) not null,
  alias                         varchar(255),
  name                          varchar(255),
  type                          varchar(255),
  description                   varchar(255),
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_skills primary key (id)
);

create table rcs_player_skills (
  id                            varchar(40) not null,
  player_id                     varchar(40) not null,
  skill_id                      varchar(255) not null,
  unlocked                      timestamp,
  active                        int default 0 not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_player_skills primary key (id),
  foreign key (player_id) references rcs_players (id) on delete restrict on update restrict,
  foreign key (skill_id) references rcs_skills (id) on delete restrict on update restrict
);

create table rcs_players (
  id                            varchar(40) not null,
  name                          varchar(255),
  level                         integer not null,
  exp                           integer not null,
  skill_points                  integer not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_players primary key (id)
);

create index ix_rcs_skills_alias on rcs_skills (alias);
create index ix_rcs_skills_name on rcs_skills (name);
