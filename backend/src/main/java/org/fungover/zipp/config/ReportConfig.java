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

    @Positive private long deleteExpiredAfterDays;

    public long getExpirationHours() {
        return expirationHours;
    }

    public void setExpirationHours(long expirationHours) {
        if (expirationHours <= 0) {
            throw new IllegalArgumentException("expirationHours must be positive");
        }
        this.expirationHours = expirationHours;
    }

    public long getDeleteExpiredAfterDays() {
        return deleteExpiredAfterDays;
    }

    public void setDeleteExpiredAfterDays(long deleteExpiredAfterDays) {
        if (deleteExpiredAfterDays <= 0) {
            throw new IllegalArgumentException("deleteExpiredAfterDays must be positive");
        }
        this.deleteExpiredAfterDays = deleteExpiredAfterDays;
    }
}
