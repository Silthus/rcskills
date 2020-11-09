-- apply changes
create table skills_player_skills (
  id                            uuid not null,
  player_id                     uuid,
  identifier                    varchar(255),
  unlocked                      boolean default false not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_skills_player_skills primary key (id)
);

create table skills_players (
  id                            uuid not null,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_skills_players primary key (id)
);

create index ix_skills_player_skills_player_id on skills_player_skills (player_id);
alter table skills_player_skills add constraint fk_skills_player_skills_player_id foreign key (player_id) references skills_players (id) on delete restrict on update restrict;

