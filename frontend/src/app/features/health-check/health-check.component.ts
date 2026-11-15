import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ActuatorHealthResponse {
  status: string;
  components?: Record<string, { status: string; details?: Record<string, unknown> }>;
}

export interface HealthCheckState {
  loading: boolean;
  error: string | null;
  data: ActuatorHealthResponse | null;
}

@Component({
  selector: 'app-health-check',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="p-4 border rounded-lg shadow-sm bg-white">
      <h2 class="text-xl font-bold mb-4">Service Health Check</h2>
      <label class="block mb-2 text-sm font-medium text-gray-700">
        Actuator Health Endpoint URL
      </label>
      <input
        type="text"
        class="w-full border rounded px-3 py-2 mb-3"
        placeholder="http://localhost:8081/actuator/health"
        [ngModel]="endpointUrl()"
        (ngModelChange)="endpointUrl.set($event)"
      />
      <button
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-medium"
        [disabled]="state().loading"
        (click)="fetchHealth()"
      >
        {{ state().loading ? 'Checking...' : 'Check Health' }}
      </button>

      @if (state().error) {
        <div class="mt-4 p-3 bg-red-100 text-red-700 rounded">
          {{ state().error }}
        </div>
      }

      @if (state().data) {
        <div class="mt-4 p-3 bg-green-100 text-green-700 rounded">
          <strong>Status:</strong> {{ state().data?.status }}
        </div>
      }
    </div>
  `,
  styles: []
})
export class HealthCheckComponent {
  private http = inject(HttpClient);

  readonly endpointUrl = signal('http://localhost:8081/actuator/health');
  readonly state = signal<HealthCheckState>({
    loading: false,
    error: null,
    data: null
  });

  fetchHealth(): void {
    this.state.update(s => ({ ...s, loading: true, error: null }));

    this.http.get<ActuatorHealthResponse>(this.endpointUrl()).subscribe({
      next: (data) => this.state.update(s => ({ ...s, loading: false, data })),
      error: (err) => this.state.update(s => ({
        ...s,
        loading: false,
        error: `Failed to fetch health: ${err.message || 'Unknown error'}`
      }))
    });
  }
}
