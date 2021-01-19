
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'APPOINTMENT_SEARCH';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('APPOINTMENT_SEARCH','appointment-management.adminFeature.AppointmentSearch.name',1,'jsp/admin/plugins/appointment-management/AppointmentSearch.jsp','appointment-management.adminFeature.AppointmentSearch.description',0,'appointment-management',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'APPOINTMENT_SEARCH';
INSERT INTO core_user_right (id_right,id_user) VALUES ('APPOINTMENT_SEARCH',1);

