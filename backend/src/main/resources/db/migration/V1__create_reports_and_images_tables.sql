CREATE TABLE report_images
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    report_id BIGINT        NOT NULL,
    CONSTRAINT pk_report_images PRIMARY KEY (id)
);

CREATE TABLE reports
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    submitted_by_user_id BIGINT       NOT NULL,
    `description`        VARCHAR(255) NOT NULL,
    event_type           VARCHAR(255) NOT NULL,
    coordinates          POINT SRID 4326       NULL,
    submitted_at         datetime     NOT NULL,
    status               VARCHAR(255) NOT NULL,
    CONSTRAINT pk_reports PRIMARY KEY (id)
);

ALTER TABLE report_images
    ADD CONSTRAINT FK_REPORT_IMAGES_ON_REPORT FOREIGN KEY (report_id) REFERENCES reports (id);