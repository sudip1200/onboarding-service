package com.rbs.customer.onboarding.onboarding_service.incident;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "incident.automation")
@Getter
@Setter
public class IncidentAutomationProperties {
    private ErrorCriticality jiraThreshold = ErrorCriticality.CRITICAL;
}
