package org.fungover.zipp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.dto.ReportType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReport() throws Exception {
        Report firstReport = new Report(1L,"Candy paper", ReportType.DEBRIS,
            50.0, 50.0, Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(post("/api/reports")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                    .attributes(user -> {
                        user.put("name", "Test User");
                        user.put("email", "test@gmail.com");
                        user.put("role", "USER");
                    }))
                .with(csrf())
            //POST isn't blocked by spring security with csfr
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(firstReport)))
            .andExpect(status().isCreated());

    }

    @Test
    void getAllReports() {
    }
}
