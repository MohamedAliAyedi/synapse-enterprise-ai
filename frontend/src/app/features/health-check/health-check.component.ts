import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthService } from '../../services/health.service';
import { HealthStatus } from '../../models/health-status.model';
import { finalize, interval, takeWhile, switchMap, catchError, of } from 'rxjs';

/**
 * HealthCheckComponent
 *
 * Displays the real-time health status of the Synapse Enterprise AI microservices.
 * Subscribes to the HealthService to poll the backend health endpoint and updates
 * the UI reactively using Angular Signals.
 */
@Component({
  selector: 'app-health-check',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './health-check.component.html',
  styles: [`
    :host {
      display: block;
      font-family: 'Inter', sans-serif;
    }
  `],
})
export class HealthCheckComponent implements OnInit, OnDestroy {
  private healthService = inject(HealthService);

  // Reactive state using Angular Signals
  readonly healthStatus = signal<HealthStatus | null>(null);
  readonly isLoading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  private unsubscribe: (() => void) | null = null;

  ngOnInit(): void {
    // Poll health status every 5 seconds
    const healthPoller = interval(5000).pipe(
      switchMap(() =>
        this.healthService.getHealth().pipe(
          catchError((err) => {
            this.error.set(err.message || 'Failed to fetch health status');
            return of(null);
          })
        )
      ),
      finalize(() => {
        this.isLoading.set(false);
      })
    );

    this.unsubscribe = healthPoller.subscribe((status) => {
      if (status) {
        this.healthStatus.set(status);
        this.error.set(null);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.unsubscribe) {
      this.unsubscribe();
    }
  }
}
