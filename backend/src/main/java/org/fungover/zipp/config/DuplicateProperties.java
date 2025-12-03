package org.fungover.zipp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "duplicate")
public class DuplicateProperties {
    private final int radiusInMeters;
    private final int timeWindowInMinutes;

    public DuplicateProperties(int radiusInMeters, int timeWindowInMinutes) {
        this.radiusInMeters = radiusInMeters;
        this.timeWindowInMinutes = timeWindowInMinutes;
    }

    public int getRadiusInMeters() {
        return radiusInMeters;
    }

    public int getTimeWindowInMinutes() {
        return timeWindowInMinutes;
    }
}

