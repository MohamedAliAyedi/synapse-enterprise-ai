import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HealthCheckComponent } from './health-check.component';
import { HealthService } from '../../services/health.service';
import { HealthStatus } from '../../models/health-status.model';

/**
 * Mock implementation of HealthService used for unit testing.
 */
class MockHealthService {
  getHealth() {
    const mockStatus: HealthStatus = { status: 'UP', details: { db: 'UP' } };
    return of(mockStatus);
  }
}

describe('HealthCheckComponent', () => {
  let component: HealthCheckComponent;
  let fixture: ComponentFixture<HealthCheckComponent>;
  let healthService: HealthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HealthCheckComponent],
      providers: [{ provide: HealthService, useClass: MockHealthService }]
    }).compileComponents();

    fixture = TestBed.createComponent(HealthCheckComponent);
    component = fixture.componentInstance;
    healthService = TestBed.inject(HealthService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call HealthService on init and set healthStatus', () => {
    const spy = spyOn(healthService, 'getHealth').and.callThrough();
    fixture.detectChanges(); // triggers ngOnInit
    expect(spy).toHaveBeenCalledTimes(1);
    expect(component.healthStatus).toEqual({ status: 'UP', details: { db: 'UP' } });
  });

  it('should handle service errors gracefully', () => {
    // Override the mock to return an error
    spyOn(healthService, 'getHealth').and.returnValue(
      throwError(() => new Error('Service unavailable'))
    );
    fixture.detectChanges(); // triggers ngOnInit
    expect(component.healthStatus).toBeUndefined();
    // Optionally, verify that an error flag is set if the component implements one
    // expect(component.error).toBeTrue();
  });
});