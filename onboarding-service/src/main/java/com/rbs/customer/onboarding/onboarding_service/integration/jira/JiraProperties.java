package com.rbs.customer.onboarding.onboarding_service.integration.jira;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integrations.jira")
@Getter
@Setter
public class JiraProperties {
    private boolean enabled = false;
    private String baseUrl;
    private String cloudId;
    private String email;
    private String apiToken;
    private String projectKey = "OBS";
    private String issueType = "Bug";
    private String createIssuePath = "/rest/api/3/issue";
}
