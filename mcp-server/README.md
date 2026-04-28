# MCP Server (Java, Spring Boot)

This is a starter MCP server to receive incidents from `onboarding-service`, enrich context from multiple systems, and produce RCA output.

## Local run

## 1) Start MCP server

```powershell
cd C:\Users\ACER\Downloads\onboarding-service\mcp-server
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\mvnw.cmd spring-boot:run
```

MCP server URL:

- `http://localhost:9090`
- Endpoint: `POST /api/v1/incidents`

## 2) Start onboarding service

```powershell
cd C:\Users\ACER\Downloads\onboarding-service\onboarding-service
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\mvnw.cmd spring-boot:run
```

In onboarding service config, keep:

- `integrations.mcp.enabled=true`
- `integrations.mcp.base-url=http://localhost:9090`
- `integrations.mcp.incident-path=/api/v1/incidents`

## Test incident endpoint directly

```http
POST http://localhost:9090/api/v1/incidents
Content-Type: application/json

{
  "timestamp": "2026-04-25T12:00:00Z",
  "criticality": "CRITICAL",
  "signal": {
    "correlationId": "postman-test-001",
    "endpoint": "POST /api/v1/onboardings",
    "errorCode": "INTERNAL_SERVER_ERROR",
    "statusCode": 500,
    "failureType": "RuntimeException",
    "message": "Sample internal failure",
    "eventTimestamp": "2026-04-25T12:00:00Z"
  },
  "enrichment": {
    "jira": {
      "projectKey": "OBS"
    },
    "git": {
      "repo": "org/repo"
    }
  }
}
```

## Connector configuration (in MCP server)

Set values in `src/main/resources/application.properties` or env vars:

- Jira: `mcp.connectors.jira.*`
- Git: `mcp.connectors.git.*`
- CloudWatch: `mcp.connectors.cloudwatch.*`
- S3: `mcp.connectors.s3.*`
- Figma: `mcp.connectors.figma.*`
- Database: `mcp.connectors.database.*`

## LLM assistant API (Postman/UI)

You can query MCP with a general question endpoint:

- `POST /api/v1/assistant/query`

Example request:

```json
{
  "question": "Analyze this Jira ticket and suggest exact code changes.",
  "jiraId": "OBS-123",
  "code": "public ResponseEntity<?> onboard(CreateOnboardingRequest req) { ... }",
  "model": "gpt-4o-mini",
  "correlationId": "abc-123",
  "context": {
    "endpoint": "POST /api/v1/onboardings",
    "statusCode": 500
  }
}
```

Notes:

- `model` is optional. If omitted, server uses `MCP_LLM_MODEL`.
- You can send only `jiraId`, or combine `jiraId` with `question`/`code`/`context`.
- The API response contains the LLM answer directly in `answer`, visible in Postman response body.

Enable by env vars:

- `MCP_LLM_ENABLED=true`
- `MCP_LLM_API_KEY=<your-api-key>`
- `MCP_LLM_MODEL=gpt-4o-mini`
- Optional `MCP_LLM_BASE_URL` for OpenAI-compatible providers

Current implementation is stub/no-op connectors by design. Replace connector implementations in `connector/impl` with real SDK/API clients.

## Real connectors now available

### Jira connector

Enable:

- `mcp.connectors.jira.enabled=true`
- `mcp.connectors.jira.base-url=https://<your-domain>.atlassian.net`
- `mcp.connectors.jira.email=<jira-email>`
- `mcp.connectors.jira.api-token=<jira-api-token>`
- `mcp.connectors.jira.project-key=<project-key>`

Behavior:

- On CRITICAL incidents, MCP creates a Jira issue via `/rest/api/2/issue`.

### CloudWatch connector

Enable:

- `mcp.connectors.cloudwatch.enabled=true`
- `mcp.connectors.cloudwatch.region=<aws-region>`
- `mcp.connectors.cloudwatch.log-group=<log-group-name>`
- `mcp.connectors.cloudwatch.lookback-minutes=120`
- `mcp.connectors.cloudwatch.limit=100`

AWS auth:

- Use default AWS credential chain (recommended IAM role or local AWS profile/environment variables).

Behavior:

- MCP queries CloudWatch Logs using `correlationId` as filter pattern and attaches matching log lines into RCA artifacts.
