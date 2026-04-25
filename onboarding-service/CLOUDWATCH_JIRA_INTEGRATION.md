# CloudWatch and Jira Integration

## 1) CloudWatch log shipping

This service writes structured logs to `stdout` using `logback-spring.xml`.

Use one of these runtime setups to push logs to CloudWatch:

- ECS/Fargate: `awslogs` log driver
- EKS: Fluent Bit -> CloudWatch Logs
- EC2/VM: CloudWatch Agent tailing application stdout/log file

The key fields already in log lines:

- `correlationId`
- `service`
- `level`
- `msg`

## 2) Jira auto ticketing

When request errors happen, the service classifies severity:

- `CRITICAL`: HTTP 5xx or `INTERNAL_SERVER_ERROR`
- `HIGH`: Not-found flows
- `MEDIUM`: Validation errors
- `LOW`: Everything else

If severity is equal or above `incident.automation.jira-threshold`, it triggers Jira ticket creation.

Configure in `application.properties`:

- `integrations.jira.enabled=true`
- `integrations.jira.base-url=https://<your-domain>.atlassian.net`
- `integrations.jira.email=<jira-user-email>`
- `integrations.jira.api-token=<jira-api-token>`
- `integrations.jira.project-key=<project-key>`
- `integrations.jira.issue-type=Bug`

## 3) Future root-cause enrichment (Jira/Figma/DB/Git/MCP)

Enrichment hooks are now available and include these sources:

- Jira (project/issue type context)
- Git (branch/commit/repo from environment variables)
- Database (runtime metadata/health)
- Figma (project/file key configuration)

Enable MCP publish in `application.properties`:

- `integrations.mcp.enabled=true`
- `integrations.mcp.base-url=http://<mcp-host>:<port>`
- `integrations.mcp.incident-path=/api/v1/incidents`

## 4) Storage mode switching (in-memory vs postgres)

Default profile is `in-memory`.

Use Postgres profile:

- `--spring.profiles.active=postgres`

And configure:

- `spring.datasource.url=jdbc:postgresql://<host>:5432/<db>`
- `spring.datasource.username=<user>`
- `spring.datasource.password=<password>`
