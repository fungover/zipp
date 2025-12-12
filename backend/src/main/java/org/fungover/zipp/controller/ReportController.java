package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.fungover.zipp.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report reportRequest,
            Authentication authentication) {
        LOG.info("Report received: {}", reportRequest);

        User currentUser = resolveCurrentUser(authentication);

        var newReport = reportService.createReport(currentUser, reportRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{userEmail}")
    public ResponseEntity<List<Report>> getAllReportsForUser(@PathVariable String userEmail) {
        return ResponseEntity.ok(reportService.getAllReportsForUser(userEmail));
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            if (email == null) {
                throw new RuntimeException("OAuth2 user has no email");
            }
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found by email: " + email));
        }

        if (principal instanceof UserDetails ud) {
            String providerId = ud.getUsername(); // ni kör providerId som username
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new RuntimeException("User not found by providerId: " + providerId));
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass());
    }
}
