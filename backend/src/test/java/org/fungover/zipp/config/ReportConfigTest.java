package org.fungover.zipp.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportConfigTest {

    @Test
    void setExpirationHours_acceptsPositiveValue() {
        ReportConfig config = new ReportConfig();

        config.setExpirationHours(5L);

        assertEquals(5L, config.getExpirationHours());
    }

    @Test
    void setExpirationHours_throwsForNonPositiveValue() {
        ReportConfig config = new ReportConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setExpirationHours(0L));
        assertThrows(IllegalArgumentException.class, () -> config.setExpirationHours(-1L));
    }

    @Test
    void setDeleteExpiredAfterDays_acceptsPositiveValue() {
        ReportConfig config = new ReportConfig();

        config.setDeleteExpiredAfterDays(10L);

        assertEquals(10L, config.getDeleteExpiredAfterDays());
    }

    @Test
    void setDeleteExpiredAfterDays_throwsForNonPositiveValue() {
        ReportConfig config = new ReportConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setDeleteExpiredAfterDays(0L));
        assertThrows(IllegalArgumentException.class, () -> config.setDeleteExpiredAfterDays(-1L));
    }
}
