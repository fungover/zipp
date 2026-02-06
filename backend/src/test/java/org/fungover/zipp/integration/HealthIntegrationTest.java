package org.fungover.zipp.integration;

import org.fungover.zipp.controller.HealthController;
import org.fungover.zipp.entity.HealthEntity;
import org.fungover.zipp.repository.HealthRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HealthIntegrationTest {

    @Autowired
    private HealthController healthController;

    @Autowired
    private HealthRepository healthRepository;

    @Test
    @Transactional
    void healthEndpointReturnsOkAndSavesToDb() {

        String response = healthController.health();

        assertThat(response).isEqualTo("OK");

        HealthEntity entity = new HealthEntity(response);
        healthRepository.save(entity);

        HealthEntity saved = healthRepository.findById(entity.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("OK");
    }
}








