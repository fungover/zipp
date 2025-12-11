package org.fungover.zipp.controller;

import org.fungover.zipp.TestcontainersConfiguration;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.dto.ReportType;
import org.fungover.zipp.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecuredReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ReportService reportService;

    @Test
    void createReportWithoutAuthorization() throws Exception {
        Report firstReport = new Report("Candy paper", ReportType.DEBRIS, 50.0, 50.0,
            Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(
                post("/api/reports")
                    .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(firstReport)))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void createReportWithAuthorization() throws Exception {
        Report firstReport = new Report("Candy paper", ReportType.DEBRIS, 50.0, 50.0,
            Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(
                post("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(
                        user -> {
                            user.put("name", "Test User");
                            user.put("email", "test@gmail.com");
                            user.put("role", "USER");
                        }))
                    .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(firstReport)))
            .andExpect(status().is2xxSuccessful());
    }

}
