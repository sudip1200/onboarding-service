package com.rbs.customer.mcp.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mcp.llm")
@Getter
@Setter
public class LlmProperties {
    private boolean enabled = false;
    private String provider = "openai";
    private String baseUrl = "https://api.openai.com";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private String systemPrompt = "You are an SRE assistant for onboarding incidents. Be concise and actionable.";
    private int timeoutSeconds = 30;
}
