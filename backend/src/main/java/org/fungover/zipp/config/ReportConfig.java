package org.fungover.zipp.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "report")
@Validated
public class ReportConfig {
    @Positive private long expirationHours;

    public long getExpirationHours() {
        return expirationHours;
    }

    public void setExpirationHours(long expirationHours) {
        if (expirationHours <= 0) {
            throw new IllegalArgumentException("expirationHours must be positive");
        }
        this.expirationHours = expirationHours;
    }
}
