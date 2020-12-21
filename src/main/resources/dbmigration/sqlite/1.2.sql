-- apply changes
create table rcs_skill_slots (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  skill_id                      varchar(40),
  status                        varchar(8),
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_rcs_skill_slots_status check ( status in ('ELIGIBLE','FREE','IN_USE')),
  constraint uq_rcs_skill_slots_skill_id unique (skill_id),
  constraint pk_rcs_skill_slots primary key (id),
  foreign key (player_id) references rcs_players (id) on delete restrict on update restrict,
  foreign key (skill_id) references rcs_player_skills (id) on delete restrict on update restrict
);

alter table rcs_skills add column no_skill_slot int default 0 not null;

