ALTER TABLE report_confirmation_entity
DROP
FOREIGN KEY FK_REPORTCONFIRMATIONENTITY_ON_REPORT;

CREATE TABLE reports_confirmations
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    report_id    BIGINT NOT NULL,
    user_id      BIGINT NULL,
    confirmed_at datetime NULL,
    CONSTRAINT pk_reports_confirmations PRIMARY KEY (id)
);

ALTER TABLE reports_confirmations
    ADD CONSTRAINT FK_REPORTS_CONFIRMATIONS_ON_REPORT FOREIGN KEY (report_id) REFERENCES reports (id);

DROP TABLE report_confirmation_entity;