CREATE TABLE report_confirmation_entity
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    report_id    BIGINT NOT NULL,
    user_id      BIGINT NULL,
    confirmed_at datetime NULL,
    CONSTRAINT pk_reportconfirmationentity PRIMARY KEY (id)
);

ALTER TABLE report_confirmation_entity
    ADD CONSTRAINT FK_REPORTCONFIRMATIONENTITY_ON_REPORT FOREIGN KEY (report_id) REFERENCES reports (id);