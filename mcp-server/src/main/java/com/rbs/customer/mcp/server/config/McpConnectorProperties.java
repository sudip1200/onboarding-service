package com.rbs.customer.mcp.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mcp.connectors")
@Getter
@Setter
public class McpConnectorProperties {

    private Jira jira = new Jira();
    private Git git = new Git();
    private CloudWatch cloudwatch = new CloudWatch();
    private S3 s3 = new S3();
    private Figma figma = new Figma();
    private Database database = new Database();

    @Getter
    @Setter
    public static class Jira {
        private boolean enabled = false;
        private String baseUrl;
        private String email;
        private String apiToken;
        private String projectKey;
    }

    @Getter
    @Setter
    public static class Git {
        private boolean enabled = false;
        private String provider;
        private String repo;
        private String apiToken;
    }

    @Getter
    @Setter
    public static class CloudWatch {
        private boolean enabled = false;
        private String region;
        private String logGroup;
        private int lookbackMinutes = 120;
        private int limit = 100;
    }

    @Getter
    @Setter
    public static class S3 {
        private boolean enabled = false;
        private String region;
        private String bucket;
        private String prefix;
    }

    @Getter
    @Setter
    public static class Figma {
        private boolean enabled = false;
        private String apiToken;
        private String fileKey;
        private String projectName;
    }

    @Getter
    @Setter
    public static class Database {
        private boolean enabled = false;
        private String jdbcUrl;
        private String username;
        private String password;
    }
}
