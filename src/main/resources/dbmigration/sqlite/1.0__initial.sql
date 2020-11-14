-- apply changes
create table rcs_player_skills (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  identifier                    varchar(255),
  name                          varchar(255),
  description                   varchar(255),
  unlocked                      timestamp,
  active                        int default 0 not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcs_player_skills primary key (id),
  foreign key (player_id) references rcs_players (id) on delete restrict on update restrict
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

