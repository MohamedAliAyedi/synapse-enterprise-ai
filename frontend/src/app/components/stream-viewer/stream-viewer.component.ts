import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, NgIf, NgFor, AsyncPipe } from '@angular/common';
import { StreamService } from '../../services/stream.service';

/**
 * StreamViewerComponent
 * ---------------------
 * Consumes the {@link StreamService} SSE endpoint and renders a live list of
 * messages. It displays a loading indicator while the connection is being
 * established and shows a user‑friendly error message if the stream fails.
 *
 * The component is implemented as a **standalone** component using Angular
 * signals for reactive state management, which aligns with the project's
 * Angular 19 standards.
 */
@Component({
  selector: 'app-stream-viewer',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, AsyncPipe],
  templateUrl: './stream-viewer.component.html',
  styleUrls: ['./stream-viewer.component.css']
})
export class StreamViewerComponent implements OnInit {
  /** Holds the streamed messages in order of arrival */
  readonly messages = signal<string[]>([]);

  /** Indicates whether the SSE connection is still being established */
  readonly loading = signal<boolean>(true);

  /** Holds an error message when the stream fails */
  readonly error = signal<string | null>(null);

  constructor(private readonly streamService: StreamService) {}

  ngOnInit(): void {
    // Subscribe to the SSE Observable provided by StreamService.
    // The service abstracts EventSource handling and returns a cold Observable.
    this.streamService.getStream().subscribe({
      next: (msg: string) => {
        // Append the new message and turn off the loading indicator.
        this.messages.update((list) => [...list, msg]);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        // Capture any error, surface a friendly message, and stop loading.
        const message = err instanceof Error ? err.message : String(err);
        this.error.set(`Streaming error: ${message}`);
        this.loading.set(false);
      },
      complete: () => {
        // Stream completed – keep the UI in a non‑loading state.
        this.loading.set(false);
      }
    });
  }
}
