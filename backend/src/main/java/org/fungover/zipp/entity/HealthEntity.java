package org.fungover.zipp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;


    @Entity
    public class HealthEntity {
        @Id
        @GeneratedValue
        private Long id;
        private String status;

        public HealthEntity() {}
        public HealthEntity(String status) { this.status = status; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

