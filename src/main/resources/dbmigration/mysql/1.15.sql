-- apply changes
alter table rcs_skill_slots drop foreign key fk_rcs_skill_slots_skill_id;
alter table rcs_skill_slots drop index uq_rcs_skill_slots_skill_id;
