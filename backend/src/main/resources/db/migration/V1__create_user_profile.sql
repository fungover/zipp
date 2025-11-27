-- Create table for storing basic user profile information
CREATE TABLE user_profile (
                              id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                              bio             TEXT NULL,
                              address         VARCHAR(100) NULL,
                              city            VARCHAR(100) NULL,
                              display_name    VARCHAR(100) NOT NULL,
                              created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
