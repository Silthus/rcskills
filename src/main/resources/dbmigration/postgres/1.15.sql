-- apply changes
alter table if exists rcs_skill_slots drop constraint if exists fk_rcs_skill_slots_skill_id;
alter table rcs_skill_slots drop constraint uq_rcs_skill_slots_skill_id;
