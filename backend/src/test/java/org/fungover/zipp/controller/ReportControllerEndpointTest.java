package org.fungover.zipp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.fungover.zipp.TestcontainersConfiguration;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.Role;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.fungover.zipp.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class ReportControllerEndpointTest {

    private User reportUser;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaTemplate<String, ReportResponse> template;

    @BeforeEach
    void setUp() {

        reportUser = new User();
        reportUser.setName("Test User");
        reportUser.setEmail("test@gmail.com");
        reportUser.setRole(Role.USER);

        userRepository.save(reportUser);
    }

    @Test
    void createReport() throws Exception {
        Report firstReport = new Report("Candy paper", ReportType.DEBRIS, 50.0, 50.0, null);

        ObjectMapper mapper = new ObjectMapper();

        CompletableFuture<SendResult<String, ReportResponse>> future = new CompletableFuture<>();
        when(template.send(anyString(), any())).thenReturn(future);

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
        Report firstReport = new Report(null, ReportType.DEBRIS, 50.0, 50.0, null);

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
        Report firstReport = new Report("Candy paper", ReportType.DEBRIS, 50.0, 50.0, null);

        reportService.createReport(reportUser, firstReport);

        mockMvc.perform(get("/api/reports").with(SecurityMockMvcRequestPostProcessors.oauth2Login().attributes(user -> {
            user.put("name", "Test User");
            user.put("email", "test@gmail.com");
            user.put("role", "USER");
        })).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
