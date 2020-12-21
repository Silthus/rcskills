-- apply changes
create table rcs_skill_slots (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  skill_id                      varchar(40),
  status                        varchar(8),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint uq_rcs_skill_slots_skill_id unique (skill_id),
  constraint pk_rcs_skill_slots primary key (id)
);

alter table rcs_skills add column no_skill_slot tinyint(1) default 0 not null;

create index ix_rcs_skill_slots_player_id on rcs_skill_slots (player_id);
alter table rcs_skill_slots add constraint fk_rcs_skill_slots_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

alter table rcs_skill_slots add constraint fk_rcs_skill_slots_skill_id foreign key (skill_id) references rcs_player_skills (id) on delete restrict on update restrict;

