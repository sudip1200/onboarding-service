package com.rbs.customer.onboarding.onboarding_service.integration.figma;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integrations.figma")
@Getter
@Setter
public class FigmaProperties {
    private boolean enabled = false;
    private String fileKey;
    private String projectName;
}
