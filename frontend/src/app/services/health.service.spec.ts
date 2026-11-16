import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HealthService } from './health.service';
import { HealthStatus } from '../models/health-status.model';

describe('HealthService', () => {
  let service: HealthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HealthService],
    });

    service = TestBed.inject(HealthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch health status successfully', (done) => {
    const mockHealthStatus: HealthStatus = {
      status: 'UP',
      service: 'ai-orchestrator-service',
      timestamp: new Date().toISOString(),
    };

    service.getHealth().subscribe((result) => {
      expect(result).toEqual(mockHealthStatus);
      done();
    });

    const req = httpMock.expectOne('http://localhost:8080/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush(mockHealthStatus);
  });

  it('should handle HTTP error gracefully', (done) => {
    service.getHealth().subscribe({
      error: (err) => {
        expect(err.status).toBe(500);
        done();
      },
    });

    const req = httpMock.expectOne('http://localhost:8080/actuator/health');
    req.flush('Internal Server Error', { status: 500, statusText: 'Internal Server Error' });
  });
});
