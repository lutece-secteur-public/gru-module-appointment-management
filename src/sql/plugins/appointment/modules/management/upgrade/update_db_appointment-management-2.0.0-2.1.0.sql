-- liquibase formatted sql
-- changeset appointment-management:update_db_appointment-management-2.0.0-2.1.0.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- LUT-35360: queue of the multiview indexer, which no longer shares core_indexer_action with the core search indexing
--
CREATE TABLE IF NOT EXISTS appointment_management_indexer_action (
  id_action INT AUTO_INCREMENT,
  id_appointment INT DEFAULT 0 NOT NULL,
  id_task INT DEFAULT 0 NOT NULL,
  PRIMARY KEY (id_action)
);

