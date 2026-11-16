import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';
import { DashboardComponent } from './dashboard.component';
import { StreamViewerComponent } from '../../components/stream-viewer/stream-viewer.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, StreamViewerComponent, DashboardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('should create the dashboard component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render app-stream-viewer component', () => {
    const streamViewerDebugEl = fixture.debugElement.query(By.directive(StreamViewerComponent));
    expect(streamViewerDebugEl).toBeTruthy();
  });
});
