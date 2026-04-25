package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.CloudWatchConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mcp.connectors.cloudwatch", name = "enabled", havingValue = "true")
public class AwsCloudWatchConnector implements CloudWatchConnector {

    private final McpConnectorProperties properties;

    @Override
    public List<String> fetchLogs(String correlationId) {
        McpConnectorProperties.CloudWatch cfg = properties.getCloudwatch();
        long end = Instant.now().toEpochMilli();
        long start = end - (cfg.getLookbackMinutes() * 60_000L);
        int limit = Math.max(1, cfg.getLimit());

        try (CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(Region.of(cfg.getRegion()))
                .build()) {
            FilterLogEventsRequest request = FilterLogEventsRequest.builder()
                    .logGroupName(cfg.getLogGroup())
                    .filterPattern("\"" + correlationId + "\"")
                    .startTime(start)
                    .endTime(end)
                    .limit(limit)
                    .build();

            List<FilteredLogEvent> events = client.filterLogEvents(request).events();
            if (events == null || events.isEmpty()) {
                return List.of("cloudwatch.no-events correlationId=" + correlationId);
            }

            return events.stream()
                    .map(e -> "ts=" + e.timestamp() + " stream=" + e.logStreamName() + " msg=" + safe(e.message()))
                    .toList();
        } catch (Exception ex) {
            log.error("cloudwatch.fetch.failed correlationId={} message={}", correlationId, ex.getMessage());
            return List.of("cloudwatch.error=" + safe(ex.getMessage()));
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
