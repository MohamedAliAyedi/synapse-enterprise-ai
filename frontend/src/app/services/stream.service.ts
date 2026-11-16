import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/**
 * Service responsible for establishing a Server‑Sent Events (SSE) connection
 * to the AI Orchestrator streaming endpoint. It exposes two observables:
 *   - `messages$` for regular data events (payload is a string)
 *   - `errors$`   for SSE error events (payload is an {@link ErrorEvent})
 *
 * The service handles automatic cleanup of the EventSource when the
 * Angular component using it is destroyed, preventing resource leaks.
 */
@Injectable({
  providedIn: 'root'
})
export class StreamService implements OnDestroy {
  /** Observable emitting data messages from the SSE stream */
  public readonly messages$: Observable<string>;
  /** Observable emitting error events from the SSE stream */
  public readonly errors$: Observable<ErrorEvent>;

  private readonly messageSubject = new Subject<string>();
  private readonly errorSubject = new Subject<ErrorEvent>();
  private eventSource: EventSource | null = null;

  constructor() {
    this.messages$ = this.messageSubject.asObservable();
    this.errors$ = this.errorSubject.asObservable();
  }

  /**
   * Opens an SSE connection to the backend streaming endpoint.
   * If a connection is already open, it will be closed first.
   */
  public connect(): void {
    // Close any existing connection before creating a new one
    this.disconnect();

    // The endpoint is proxied through the API gateway; adjust the path if needed.
    this.eventSource = new EventSource('/api/stream');

    // Normal data events – the backend sends plain text payloads.
    this.eventSource.onmessage = (event: MessageEvent) => {
      this.messageSubject.next(event.data);
    };

    // Error handling – the backend now emits SSE error events (see docs/ERROR_HANDLING.md).
    this.eventSource.onerror = (error: Event) => {
      // Convert generic Event to ErrorEvent for richer typing when possible.
      const errEvent = error as ErrorEvent;
      this.errorSubject.next(errEvent);
      // Optionally close the connection after an error to allow a fresh reconnect.
      this.disconnect();
    };
  }

  /**
   * Gracefully closes the SSE connection and cleans up resources.
   */
  public disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
    // Complete subjects so subscribers know the stream ended.
    this.messageSubject.complete();
    this.errorSubject.complete();
  }

  /**
   * Angular lifecycle hook – ensures the EventSource is closed when the
   * service is destroyed (e.g., on application shutdown).
   */
  ngOnDestroy(): void {
    this.disconnect();
  }
}
