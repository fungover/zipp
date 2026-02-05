package org.fungover.zipp.integration;

import org.fungover.zipp.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HealthIntegrationTest {

    @Autowired
    private HealthController healthController;

    @Test
    void healthEndpointReturnsOk() {
        String response = healthController.health();

        assertThat(response).isEqualTo("OK");
    }
}



