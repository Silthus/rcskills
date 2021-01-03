-- apply changes
alter table rcs_players add column settings_id varchar(40);

alter table rcs_players add constraint fk_rcs_players_settings_id foreign key (settings_id) references rcs_datastore (id) on delete restrict on update restrict;

