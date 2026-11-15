# Error Handling in AiOrchestratorService

This document outlines the error handling strategy for the **AiOrchestratorService** and related components. It is intended for developers writing integration tests, extending the service, or troubleshooting runtime issues.

## Overview

- **Prompt validation** – The service validates the incoming prompt before invoking the AI model. If the prompt is `null` or blank, an `IllegalArgumentException` is thrown.
- **Model availability guard** – A guard ensures that the configured AI model is not `null`. If it is, a `IllegalStateException` is raised.
- **Streaming SSE emitter** – The `StreamController` always completes the `SseEmitter` (or closes the `ResponseBodyEmitter`) in a `finally` block to avoid resource leaks.
- **External API failures** – Exceptions from the Groq API are caught and wrapped in a `AiOrchestratorException` (a runtime exception) with a meaningful message and the original cause.
- **Kafka & Redis interactions** – Failures when publishing to Kafka or caching in Redis are logged and re‑thrown as `AiOrchestratorException` to surface the problem to the caller.

## Detailed Flow

1. **Incoming request** (`/stream?prompt=...`)
   - `StreamController` validates the `prompt` query parameter.
   - If invalid, returns `400 Bad Request` with an error JSON payload.
2. **Service invocation** (`AiOrchestratorService.streamResponse(prompt)`)
   - Checks the model configuration; throws `IllegalStateException` if missing.
   - Calls the Spring AI `ChatModel` (Groq) inside a `try/catch`.
   - On `ChatModel` failure, wraps the exception.
3. **Event publishing**
   - Publishes a `UserPromptEvent` to Kafka.
   - Caches the prompt in Redis for correlation.
   - Any failure here propagates as `AiOrchestratorException`.
4. **SSE emission**
   - The controller creates an `SseEmitter` with a timeout.
   - The service streams tokens; each token is sent via `emitter.send()`.
   - In a `finally` block, `emitter.complete()` (or `emitter.completeWithError(e)`) ensures the connection is closed.

## Expected Error Responses

| Situation | HTTP Status | Body Example |
|-----------|-------------|--------------|
| Missing/blank `prompt` query param | `400` | `{ "error": "Prompt must not be empty" }` |
| No AI model configured | `500` | `{ "error": "AI model is not configured" }` |
| Groq API failure (network, auth, quota) | `502` | `{ "error": "Failed to communicate with AI provider", "details": "<original message>" }` |
| Kafka publish failure | `503` | `{ "error": "Event bus unavailable", "details": "<original message>" }` |
| Redis cache failure | `503` | `{ "error": "Cache unavailable", "details": "<original message>" }` |

## Logging

- All validation failures are logged at **WARN** level.
- External service errors are logged at **ERROR** level with stack traces.
- Successful streaming events are logged at **INFO** level with correlation IDs.

## Testing Guidance

When writing integration tests (e.g., `AiOrchestratorServiceIT`), simulate the following error conditions:

- **Invalid prompt** – send an empty string and assert `400` response.
- **Missing model** – override `AiOrchestratorConfig` bean to return `null` and expect `500`.
- **Groq failure** – mock `ChatModel` to throw `RuntimeException` and assert `502`.
- **Kafka down** – start the test with an embedded Kafka that is stopped before the request; expect `503`.
- **Redis down** – disable the embedded Redis; expect `503`.

Each test should verify that the SSE emitter is **always completed** (no hanging connections) and that the appropriate error payload is returned.
