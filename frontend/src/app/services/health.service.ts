import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HealthStatus } from '../models/health-status.model';

/**
 * Service responsible for fetching the health status of the backend services.
 * It calls the API gateway's health endpoint which aggregates health information
 * from all microservices.
 */
@Injectable({
  providedIn: 'root'
})
export class HealthService {
  /** Base URL for the health endpoint; adjust if gateway path changes */
  private readonly healthUrl = '/api/health';

  constructor(private http: HttpClient) {}

  /**
   * Retrieves the current health status.
   * @returns Observable emitting a {@link HealthStatus} object.
   */
  getHealth(): Observable<HealthStatus> {
    return this.http.get<HealthStatus>(this.healthUrl);
  }
}
