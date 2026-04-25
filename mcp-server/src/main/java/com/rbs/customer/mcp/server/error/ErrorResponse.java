package com.rbs.customer.mcp.server.error;

import java.time.Instant;

public record ErrorResponse(
        String errorCode,
        String message,
        Instant timestamp
) {
}
