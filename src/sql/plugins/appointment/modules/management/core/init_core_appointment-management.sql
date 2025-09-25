-- liquibase formatted sql
-- changeset appointment-management:init_core_appointment-management.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'MULTIVIEW_APPOINTMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('MULTIVIEW_APPOINTMENT','module.appointment.management.adminFeature.MultiviewAppointment.name',2,'jsp/admin/plugins/appointment/modules/management/MultiviewAppointment.jsp','module.appointment.management.adminFeature.MultiviewAppointment.description',0,'appointment-management',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'MULTIVIEW_APPOINTMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('MULTIVIEW_APPOINTMENT',1);

