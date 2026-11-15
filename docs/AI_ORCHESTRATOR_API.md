# AI Orchestrator Service API

## Streaming Endpoint

`GET /stream`

**Description**: Initiates a Server‑Sent Events (SSE) stream that delivers AI‑generated responses in real time. The endpoint uses `AiOrchestratorService` to process the incoming prompt and streams partial results back to the client.

**Headers**:
- `Accept: text/event-stream`
- `Authorization: Bearer <jwt>`

**Query Parameters**:
- `prompt` (required) – The user prompt to be processed by the AI model.

**Response**:
- `Content-Type: text/event-stream`
- Each event contains a JSON payload, e.g.:
```json
{ "content": "partial text...", "type": "partial" }
```
  - `type` can be `partial` for intermediate chunks or `complete` when the final answer is sent.

**Error Handling**:
- `400 Bad Request` – Missing or empty `prompt` parameter.
- `401 Unauthorized` – Missing or invalid JWT.
- `500 Internal Server Error` – Unexpected failure during processing.

**Example (cURL)**:
```bash
curl -N \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/stream?prompt=Explain+quantum+computing"
```
The `-N` flag tells `curl` to disable buffering so events are printed as they arrive.

**Implementation Notes**:
- The controller creates an `SseEmitter` with a reasonable timeout (e.g., 5 minutes).
- `AiOrchestratorService` streams tokens via a reactive `Flux<String>`; each token is sent to the emitter.
- The emitter is always completed or completed exceptionally to avoid resource leaks.
- Rate‑limiting and JWT validation are handled by the API gateway.

**Future Enhancements**:
- Add support for client‑side cancellation via the SSE `close` event.
- Document additional query parameters for model selection and temperature control.
