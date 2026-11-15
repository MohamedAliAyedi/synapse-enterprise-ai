# Streaming API Documentation

The **AI Orchestrator Service** exposes a Server‑Sent Events (SSE) endpoint that streams AI model responses in real‑time. This allows front‑end clients to render partial answers as they are generated, providing a responsive user experience.

## Endpoint

```
GET /api/v1/stream
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `prompt`  | string | ✅ | The user prompt to send to the LLM. |
| `model`   | string | ❌ | Optional model identifier (defaults to the configured default). |
| `sessionId` | string | ❌ | Optional session identifier for conversation context. |

### Headers
| Header | Value |
|--------|-------|
| `Accept` | `text/event-stream` |
| `Authorization` | `Bearer <JWT>` (if gateway security is enabled) |

## Response Format

The endpoint streams events conforming to the SSE specification. Each event contains a JSON payload with the following structure:

```json
{
  "type": "chunk" | "error" | "complete",
  "content": "string", // partial answer for type=chunk
  "error": {
    "code": "string",
    "message": "string"
  } // present only for type=error
}
```

- **type**: Indicates the nature of the event.
  - `chunk` – A partial segment of the generated answer.
  - `error` – An error occurred; processing stops.
  - `complete` – The generation is finished; no further events will be sent.
- **content**: The text fragment for `chunk` events.
- **error**: Detailed error information for `error` events.

### Example Stream
```
event: message
data: {"type":"chunk","content":"Hello"}

event: message
data: {"type":"chunk","content":", world!"}

event: message
data: {"type":"complete"}
```

## Error Handling

If the LLM service returns an error or the request is malformed, the stream will emit a single `error` event and then close the connection. Clients should listen for this event to display appropriate feedback.

## Client Integration (Angular)

```ts
const eventSource = new EventSource(`/api/v1/stream?prompt=${encodeURIComponent(prompt)}`);

eventSource.addEventListener('message', (e) => {
  const data = JSON.parse(e.data);
  switch (data.type) {
    case 'chunk':
      // Append data.content to UI
      break;
    case 'error':
      // Show data.error.message
      eventSource.close();
      break;
    case 'complete':
      // Finalize UI state
      eventSource.close();
      break;
  }
});
```

---

For more details on the underlying implementation, see `services/ai-orchestrator-service/src/main/java/com/example/aiorchestrator/controller/StreamController.java`.
