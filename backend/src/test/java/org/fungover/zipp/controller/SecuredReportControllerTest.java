package org.fungover.zipp.controller;

import org.fungover.zipp.TestcontainersConfiguration;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.Role;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecuredReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaTemplate<String, ReportResponse> kafkaTemplate;

    @MockitoBean
    private KafkaAdmin kafkaAdmin;

    private User user;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        user.setProviderId("test-provider-123");
        user.setProvider("google");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        CompletableFuture<SendResult<String, ReportResponse>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), any(ReportResponse.class))).thenReturn(future);
    }

    @Test
    void createReportWithoutAuthorization() throws Exception {
        Report firstReport = new Report(user, "Candy paper", ReportType.DEBRIS, 50.0, 50.0,
                Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        mockMvc.perform(post("/api/reports").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(firstReport))).andExpect(status().is3xxRedirection());
    }

    @Test
    void createReportWithAuthorization() throws Exception {
        Report firstReport = new Report(user, "Candy paper", ReportType.DEBRIS, 50.0, 50.0,
                Instant.parse("2025-12-03T15:30:00Z"), ReportStatus.ACTIVE, null);

        mockMvc.perform(post("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(u -> {
            u.put("name", "Test User");
            u.put("email", "test@gmail.com");
            u.put("sub", user.getProviderId());
        })).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(firstReport)))
                .andExpect(status().isCreated());
    }
}
