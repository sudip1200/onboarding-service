package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DatabaseContextEnricher implements IncidentContextEnricher {

    private final DataSource dataSource;

    @Override
    public String source() {
        return "database";
    }

    @Override
    public Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality) {
        Map<String, Object> context = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            context.put("product", metaData.getDatabaseProductName());
            context.put("version", metaData.getDatabaseProductVersion());
            context.put("url", metaData.getURL());
            context.put("validation", "ok");
        } catch (Exception ex) {
            context.put("validation", "failed");
            context.put("error", ex.getMessage());
        }
        return context;
    }
}
