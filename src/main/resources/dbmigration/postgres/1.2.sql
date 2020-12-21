-- apply changes
create table rcs_skill_slots (
  id                            uuid not null,
  player_id                     uuid,
  skill_id                      uuid,
  status                        varchar(8),
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint ck_rcs_skill_slots_status check ( status in ('ELIGIBLE','FREE','IN_USE')),
  constraint uq_rcs_skill_slots_skill_id unique (skill_id),
  constraint pk_rcs_skill_slots primary key (id)
);

alter table rcs_skills add column no_skill_slot boolean default false not null;

create index ix_rcs_skill_slots_player_id on rcs_skill_slots (player_id);
alter table rcs_skill_slots add constraint fk_rcs_skill_slots_player_id foreign key (player_id) references rcs_players (id) on delete restrict on update restrict;

alter table rcs_skill_slots add constraint fk_rcs_skill_slots_skill_id foreign key (skill_id) references rcs_player_skills (id) on delete restrict on update restrict;

