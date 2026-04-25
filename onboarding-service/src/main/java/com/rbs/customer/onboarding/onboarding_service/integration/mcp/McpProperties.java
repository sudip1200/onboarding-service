package com.rbs.customer.onboarding.onboarding_service.integration.mcp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integrations.mcp")
@Getter
@Setter
public class McpProperties {
    private boolean enabled = false;
    private String baseUrl;
    private String incidentPath = "/api/v1/incidents";
}
