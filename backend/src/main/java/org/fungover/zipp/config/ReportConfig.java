package org.fungover.zipp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "report")
public class ReportConfig {
  private long expirationHours;

  public long getExpirationHours() {
    return expirationHours;
  }

  public void setExpirationHours(long expirationHours) {
    this.expirationHours = expirationHours;
  }
}