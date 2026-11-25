-- liquibase formatted sql
-- changeset appointment-management:create_db_appointment-management.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Structure for table appointment_management_indexer_action (queue of the multiview indexer)
--
DROP TABLE IF EXISTS appointment_management_indexer_action;
CREATE TABLE appointment_management_indexer_action (
  id_action INT AUTO_INCREMENT,
  id_appointment INT DEFAULT 0 NOT NULL,
  id_task INT DEFAULT 0 NOT NULL,
  PRIMARY KEY (id_action)
);
