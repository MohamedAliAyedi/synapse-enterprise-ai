import { Component, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';

/**
 * Component displaying the health status of backend services.
 */
@Component({
  selector: 'app-health-check',
  templateUrl: './health-check.component.html',
  styleUrls: ['./health-check.component.css'],
  standalone: true,
})
export class HealthCheckComponent {
  /** Signal holding the current health status message */
  readonly status = signal<string>('Checking...');

  constructor(private readonly http: HttpClient) {
    this.checkHealth();
  }

  /** Calls the health endpoint and updates the status signal */
  private checkHealth(): void {
    this.http
      .get<{ status: string }>('/api/health', { responseType: 'json' })
      .pipe(
        catchError((error) => {
          console.error('Health check failed', error);
          return of({ status: 'Unavailable' });
        })
      )
      .subscribe((result) => this.status.set(result.status));
  }
}
