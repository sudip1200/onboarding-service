package com.rbs.customer.mcp.server.service;

import com.rbs.customer.mcp.server.config.LlmProperties;
import com.rbs.customer.mcp.server.connector.JiraConnector;
import com.rbs.customer.mcp.server.model.LlmQueryRequest;
import com.rbs.customer.mcp.server.model.LlmQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiCompatibleLlmAssistantService implements LlmAssistantService {

    private final LlmProperties properties;
    private final JiraConnector jiraConnector;

    @Override
    public LlmQueryResponse ask(LlmQueryRequest request) {
        if (!properties.isEnabled()) {
            return fallback("LLM is disabled. Set MCP_LLM_ENABLED=true to enable.");
        }
        if (isBlank(properties.getApiKey())) {
            return fallback("LLM API key is missing. Set MCP_LLM_API_KEY.");
        }

        String provider = safe(properties.getProvider(), "openai");
        String model = resolveModel(request);
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        String apiMode = resolveApiMode(baseUrl, provider, model);
        String url = resolveEndpoint(baseUrl, apiMode);
        String input = buildInput(request);

        if (isBlank(input)) {
            return fallback("Request is empty. Provide question, ticket, or code for analysis.");
        }

        Map<String, Object> payload = buildPayload(model, input, apiMode);

        try {
            RestClient.RequestBodySpec requestSpec = RestClient.create()
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            if (isAnthropicMode(apiMode)) {
                requestSpec = requestSpec
                        .header("x-api-key", properties.getApiKey())
                        .header("anthropic-version", "2023-06-01");
            } else {
                requestSpec = requestSpec
                        .header("Authorization", "Bearer " + properties.getApiKey());
            }

            Map<String, Object> body = requestSpec.body(payload).retrieve().body(Map.class);

            String answer = extractOutputText(body, apiMode);
            if (isBlank(answer)) {
                answer = "LLM request succeeded but returned empty output.";
            }
            return new LlmQueryResponse(
                    provider,
                    model,
                    answer,
                    false,
                    Instant.now(),
                    Map.of("source", "api", "endpoint", url)
            );
        } catch (Exception ex) {
            log.error("llm.request.failed provider={} model={} message={}", provider, model, ex.getMessage());
            return fallback("LLM request failed: " + safe(ex.getMessage(), "unknown error"));
        }
    }

    private String resolveEndpoint(String baseUrl, String apiMode) {
        if (isAnthropicMode(apiMode)) {
            return baseUrl + "/v1/messages";
        }
        if (isChatCompletionsMode(apiMode)) {
            return baseUrl + "/chat/completions";
        }
        return baseUrl + "/v1/responses";
    }

    private Map<String, Object> buildPayload(String model, String input, String apiMode) {
        if (isAnthropicMode(apiMode)) {
            return buildAnthropicMessagesPayload(model, input);
        }
        if (isChatCompletionsMode(apiMode)) {
            return buildChatCompletionsPayload(model, input);
        }
        return Map.of(
                "model", model,
                "instructions", safe(properties.getSystemPrompt(), ""),
                "input", input
        );
    }

    private String buildInput(LlmQueryRequest request) {
        StringBuilder text = new StringBuilder();
        if (!isBlank(request.question())) {
            text.append("Question: ").append(request.question().trim());
        }
        if (!isBlank(request.jiraId())) {
            appendSection(text, "JiraId", request.jiraId());
            Map<String, Object> jiraContext = jiraConnector.fetchTicketByKey(request.jiraId().trim());
            appendSection(text, "JiraTicket", jiraContext.toString());
        }
        if (!isBlank(request.ticket())) {
            appendSection(text, "Ticket", request.ticket());
        }
        if (!isBlank(request.code())) {
            appendSection(text, "Code", request.code());
        }
        if (!isBlank(request.correlationId())) {
            appendSection(text, "CorrelationId", request.correlationId());
        }
        if (request.context() != null && !request.context().isEmpty()) {
            appendSection(text, "Context", request.context().toString());
        }
        return text.toString();
    }

    private String resolveModel(LlmQueryRequest request) {
        if (request != null && !isBlank(request.model())) {
            return request.model().trim();
        }
        return safe(properties.getModel(), "gpt-4o-mini");
    }

    private void appendSection(StringBuilder text, String section, String value) {
        if (!text.isEmpty()) {
            text.append("\n");
        }
        text.append(section).append(": ").append(value.trim());
    }

    private Map<String, Object> buildChatCompletionsPayload(String model, String input) {
        String systemPrompt = safe(properties.getSystemPrompt(), "");
        if (isBlank(systemPrompt)) {
            return Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", input)
                    )
            );
        }
        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", input)
                )
        );
    }

    private Map<String, Object> buildAnthropicMessagesPayload(String model, String input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("max_tokens", 1024);
        payload.put("messages", List.of(
                Map.of("role", "user", "content", input)
        ));
        String systemPrompt = safe(properties.getSystemPrompt(), "");
        if (!isBlank(systemPrompt)) {
            payload.put("system", systemPrompt);
        }
        return payload;
    }

    private String extractOutputText(Map<String, Object> body, String apiMode) {
        if (isAnthropicMode(apiMode)) {
            return extractAnthropicText(body);
        }
        if (isChatCompletionsMode(apiMode)) {
            return extractChatCompletionsText(body);
        }
        return extractResponsesApiText(body);
    }

    private String extractResponsesApiText(Map<String, Object> body) {
        if (body == null) {
            return "";
        }
        Object outputText = body.get("output_text");
        if (outputText instanceof String text) {
            return text;
        }

        Object output = body.get("output");
        if (!(output instanceof Iterable<?> items)) {
            return "";
        }

        StringBuilder merged = new StringBuilder();
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> itemMap)) {
                continue;
            }
            Object contentObj = itemMap.get("content");
            if (!(contentObj instanceof Iterable<?> contents)) {
                continue;
            }
            for (Object content : contents) {
                if (!(content instanceof Map<?, ?> contentMap)) {
                    continue;
                }
                Object textObj = contentMap.get("text");
                if (textObj instanceof String text && !text.isBlank()) {
                    if (!merged.isEmpty()) {
                        merged.append("\n");
                    }
                    merged.append(text);
                }
            }
        }
        return merged.toString();
    }

    private String extractChatCompletionsText(Map<String, Object> body) {
        if (body == null) {
            return "";
        }
        Object choices = body.get("choices");
        if (!(choices instanceof Iterable<?> iterableChoices)) {
            return "";
        }
        for (Object choice : iterableChoices) {
            if (!(choice instanceof Map<?, ?> choiceMap)) {
                continue;
            }
            Object message = choiceMap.get("message");
            if (!(message instanceof Map<?, ?> messageMap)) {
                continue;
            }
            Object content = messageMap.get("content");
            if (content instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private String extractAnthropicText(Map<String, Object> body) {
        if (body == null) {
            return "";
        }
        Object content = body.get("content");
        if (!(content instanceof Iterable<?> blocks)) {
            return "";
        }

        StringBuilder merged = new StringBuilder();
        for (Object block : blocks) {
            if (!(block instanceof Map<?, ?> blockMap)) {
                continue;
            }
            Object type = blockMap.get("type");
            Object textObj = blockMap.get("text");
            if ("text".equals(type) && textObj instanceof String text && !text.isBlank()) {
                if (!merged.isEmpty()) {
                    merged.append("\n");
                }
                merged.append(text);
            }
        }
        return merged.toString();
    }

    private LlmQueryResponse fallback(String message) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "fallback");
        metadata.put("enabled", properties.isEnabled());
        return new LlmQueryResponse(
                safe(properties.getProvider(), "openai"),
                safe(properties.getModel(), "gpt-4o-mini"),
                message,
                true,
                Instant.now(),
                metadata
        );
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = safe(baseUrl, "https://api.openai.com");
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String resolveApiMode(String baseUrl, String provider, String model) {
        String providerLc = safe(provider, "").toLowerCase();
        String modelLc = safe(model, "").toLowerCase();
        String baseUrlLc = safe(baseUrl, "").toLowerCase();
        if (providerLc.contains("anthropic")
                || modelLc.startsWith("claude")
                || baseUrlLc.contains("api.anthropic.com")) {
            return "anthropic";
        }
        if (providerLc.contains("ollama")
                || baseUrlLc.contains("localhost:11434")
                || baseUrlLc.contains("127.0.0.1:11434")) {
            return "chat_completions";
        }
        if (providerLc.contains("gemini")
                || modelLc.startsWith("gemini")
                || baseUrlLc.contains("generativelanguage.googleapis.com")
                || baseUrlLc.endsWith("/openai")) {
            return "chat_completions";
        }
        return "responses";
    }

    private boolean isAnthropicMode(String apiMode) {
        return "anthropic".equals(apiMode);
    }

    private boolean isChatCompletionsMode(String apiMode) {
        return "chat_completions".equals(apiMode);
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
