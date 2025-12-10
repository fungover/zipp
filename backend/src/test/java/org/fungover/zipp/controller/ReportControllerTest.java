package org.fungover.zipp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Test
    void createReport() throws Exception {
        Report firstReport = new Report(1L, "Candy paper", ReportType.DEBRIS, 50.0, 50.0,
                Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(
                post("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(user -> {
                    user.put("name", "Test User");
                    user.put("email", "test@gmail.com");
                    user.put("role", "USER");
                })).with(csrf())
                        // POST isn't blocked by spring security with csrf
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(firstReport)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.description").value("Candy paper"));
    }

    @Test
    void throwsErrorWhenRequestIsNotValid() throws Exception {
        Report firstReport = new Report(1L, null, ReportType.DEBRIS, 50.0, 50.0, Instant.parse("2025-12-03T15:30:00Z"),
                ReportStatus.ACTIVE, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(
                post("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(user -> {
                    user.put("name", "Test User");
                    user.put("email", "test@gmail.com");
                    user.put("role", "USER");
                })).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(firstReport)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsSubmittedReport() throws Exception {
        Report firstReport = new Report(1L, "Candy paper", ReportType.DEBRIS, 50.0, 50.0,
                Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        reportService.createReport(firstReport);

        mockMvc.perform(get("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(user -> {
            user.put("name", "Test User");
            user.put("email", "test@gmail.com");
            user.put("role", "USER");
        })).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
